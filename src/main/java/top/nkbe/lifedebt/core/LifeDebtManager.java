package top.nkbe.lifedebt.core;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命债系统的死亡处理中枢。所有致命伤的判定都收敛到这里，
 * 避免把 {@code if (hasTotem)} 这类判断散落在各个 mixin 中。
 *
 * <p>核心流程：
 * <pre>
 *   玩家受到致命伤 → handleDeath()
 *        ↓ 有剩余借命容量
 *   取消死亡 + 债务+1 + 容量-1 + 借命次数+1 + 生命上限-3（最低2）
 *        ↓
 *   同步（actionbar 提示剩余借命）
 * </pre>
 */
public final class LifeDebtManager {

	private static final Logger LOGGER = LoggerFactory.getLogger("lifedebt");

	/** 每借一次命扣除的生命上限（半心为单位，3 = 1.5 颗心）。 */
	private static final double MAX_HEALTH_PENALTY_PER_BORROW = 3.0;

	/** 生命上限扣除后的下限，避免归零导致玩家无法存活。 */
	private static final double MIN_MAX_HEALTH = 2.0;

	/**
	 * 借命生命上限惩罚所用的属性修饰符 ID。与旧 1.x 休眠系统的
	 * {@code max_health_penalty} 区分，两者互不干扰。
	 */
	private static final Identifier BORROW_PENALTY_ID =
			Identifier.of("lifedebt", "borrow_max_health_penalty");

	private LifeDebtManager() {
	}

	/**
	 * 处理一次本应致命的伤害。
	 *
	 * @return {@code true} 表示命债系统已介入并阻止死亡；{@code false} 表示放行，玩家正常死亡。
	 */
	public static boolean handleDeath(ServerPlayerEntity player) {
		LifeDebtData data = LifeDebtAttachments.get(player);

		// 图腾容量已耗尽：命债无法介入，玩家正常死亡（进入清算/常规死亡）。
		if (data.getTotemCharge() <= 0) {
			return false;
		}

		// 借命：消耗一点图腾容量，累积一笔债务。
		data.setTotemCharge(data.getTotemCharge() - 1);
		data.setBorrowedLife(data.getBorrowedLife() + 1);
		data.setDeathCount(data.getDeathCount() + 1);
		data.addDebt(1);

		// 借命的代价：按累计借命次数永久压低生命上限（最低 2）。
		// TODO(偿还机制)：还债时应回调此处逐步恢复生命上限；恢复规则待定。
		applyMaxHealthPenalty(player, data.getBorrowedLife());

		// 恢复到（已压低的）存活状态，取消这次死亡。
		player.setHealth(player.getMaxHealth());

		LOGGER.info("[借命] {} 借命成功：剩余容量={}，债务={}，累计借命={}，生命上限={}",
				player.getName().getString(), data.getTotemCharge(), data.getDebt(),
				data.getBorrowedLife(), player.getMaxHealth());
		notifyBorrow(player, data);
		return true;
	}

	/**
	 * 依累计借命次数设置生命上限惩罚：惩罚总量 = 借命次数 × {@value #MAX_HEALTH_PENALTY_PER_BORROW}，
	 * 并夹取使生命上限不低于 {@value #MIN_MAX_HEALTH}。用单一可替换的修饰符表示当前总惩罚，
	 * 每次借命重算，避免叠加多个修饰符导致状态难以追踪。
	 */
	private static void applyMaxHealthPenalty(ServerPlayerEntity player, int borrowedLife) {
		EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (attr == null) {
			return;
		}

		attr.removeModifier(BORROW_PENALTY_ID);

		double maxPenalty = attr.getBaseValue() - MIN_MAX_HEALTH;
		double penalty = Math.min(MAX_HEALTH_PENALTY_PER_BORROW * borrowedLife, maxPenalty);
		if (penalty <= 0.0) {
			return;
		}

		attr.addPersistentModifier(new EntityAttributeModifier(
				BORROW_PENALTY_ID, -penalty, EntityAttributeModifier.Operation.ADD_VALUE));
	}

	/** 向玩家显示当前借命状态（actionbar）。 */
	private static void notifyBorrow(ServerPlayerEntity player, LifeDebtData data) {
		player.sendMessage(
				Text.translatable("lifedebt.message.borrowed", data.getTotemCharge(), data.getDebt()),
				true);
	}
}
