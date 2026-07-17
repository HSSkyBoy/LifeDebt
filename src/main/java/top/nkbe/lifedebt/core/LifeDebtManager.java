package top.nkbe.lifedebt.core;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * 命债系统的死亡处理中枢。所有致命伤的判定都收敛到这里，
 * 避免把 {@code if (hasTotem)} 这类判断散落在各个 mixin 中。
 *
 * <p>核心流程（Alpha 最小闭环）：
 * <pre>
 *   玩家受到致命伤 → handleDeath()
 *        ↓ 有剩余借命容量
 *   取消死亡 + 债务+1 + 容量-1 + 借命次数+1
 *        ↓
 *   同步（actionbar 提示剩余借命）
 * </pre>
 */
public final class LifeDebtManager {

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
		// 注：Alpha 阶段签约即获得容量、暂不区分契约类型；契约效果于 Milestone 2 接入。
		if (data.getTotemCharge() <= 0) {
			return false;
		}

		// 借命：消耗一点图腾容量，累积一笔债务。
		data.setTotemCharge(data.getTotemCharge() - 1);
		data.setBorrowedLife(data.getBorrowedLife() + 1);
		data.setDeathCount(data.getDeathCount() + 1);
		data.addDebt(1);

		// 恢复到存活状态，取消这次死亡。
		player.setHealth(player.getMaxHealth());

		notifyBorrow(player, data);
		return true;
	}

	/** 向玩家显示当前借命状态（Alpha 阶段用 actionbar，HUD 于 Milestone 2 接入）。 */
	private static void notifyBorrow(ServerPlayerEntity player, LifeDebtData data) {
		player.sendMessage(
				Text.translatable("lifedebt.message.borrowed", data.getTotemCharge(), data.getDebt()),
				true);
	}
}
