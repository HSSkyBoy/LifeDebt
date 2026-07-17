package top.nkbe.lifedebt.util;

import top.nkbe.lifedebt.advancement.ModAdvancements;
import top.nkbe.lifedebt.damage.LifeDebtDamageTypes;
import top.nkbe.lifedebt.player.LifeDebtPlayerAccess;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
//? if >=1.21.2 {
/*import net.minecraft.server.world.ServerWorld;
*///?}

/**
 * 不屈 buff 结束时的生命上限惩罚逻辑。
 */
public final class LifeDebtPenaltyHandler {

	private LifeDebtPenaltyHandler() {
	}

	/**
	 * 在不屈 buff 获得时初始化会话：清除残留惩罚，新开 buff 时重置死亡计数。
	 *
	 * @param player 玩家实体
	 */
	public static void handleEffectApplied(PlayerEntity player) {
		//? if <1.18 {
		/*// <1.18：Entity 尚无 getWorld()，使用 getEntityWorld()。
		if (player.getEntityWorld().isClient || !(player instanceof LifeDebtPlayerAccess)) {
		*///?} else {
		if (LifeDebtWorldHelper.isClient(player) || !(player instanceof LifeDebtPlayerAccess)) {
		//?}
			return;
		}

		LifeDebtPlayerAccess access = (LifeDebtPlayerAccess) player;
		access.lifedebt$setEffectEndSettled(false);
		access.lifedebt$clearMaxHealthPenalty();

		if (!access.lifedebt$isBuffSessionActive()) {
			access.lifedebt$setDeathCount(0);
		}

		access.lifedebt$setBuffSessionActive(true);
	}

	/**
	 * 在不屈效果被移除时尝试结算生命上限惩罚。
	 *
	 * @param player 玩家实体
	 */
	public static void handleEffectEnd(PlayerEntity player) {
		//? if <1.18 {
		/*if (player.getEntityWorld().isClient) {
		*///?} else {
		if (LifeDebtWorldHelper.isClient(player)) {
		//?}
			return;
		}

		if (!(player instanceof LifeDebtPlayerAccess)) {
			return;
		}

		LifeDebtPlayerAccess access = (LifeDebtPlayerAccess) player;
		if (access.lifedebt$isEffectEndSettled() || access.lifedebt$isRefreshingBuff()) {
			return;
		}

		int deathCount = access.lifedebt$getDeathCount();

		access.lifedebt$clearMaxHealthPenalty();
		//? if <1.21.2 {
		EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		//?} else {
		/*// >=1.21.2：属性常量更名（GENERIC_ 前缀移除）。
		EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
		*///?}
		double baseMax = maxHealthAttr != null ? maxHealthAttr.getBaseValue() : player.getMaxHealth();

		access.lifedebt$setEffectEndSettled(true);
		access.lifedebt$setBuffSessionActive(false);
		access.lifedebt$setDeathCount(0);

		if (deathCount <= 0) {
			return;
		}

		if (baseMax - deathCount <= 0.0) {
			access.lifedebt$applyMaxHealthPenalty(deathCount);
			if (player instanceof ServerPlayerEntity) {
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				ModAdvancements.grantBurnOutBody(serverPlayer);
			}
			killFromBurnOutPenalty(player);
			return;
		}

		access.lifedebt$applyMaxHealthPenalty(deathCount);
		player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
	}

	/**
	 * 以「燃尽」伤害类型击杀玩家（死亡信息：%1$s燃尽了）。
	 * 结算前已设置 {@code isEffectEndSettled}，免死 mixin 不会再次触发，避免 ConcurrentModificationException。
	 *
	 * @param player 玩家实体
	 */
	private static void killFromBurnOutPenalty(PlayerEntity player) {
		//? if <1.19.4 {
		/*// <1.19.4：Entity#getDamageSources 尚不存在，直接使用静态 DamageSource 单例。
		DamageSource source = LifeDebtDamageTypes.BURN_OUT;
		*///?} else {
		DamageSource source = player.getDamageSources().create(LifeDebtDamageTypes.BURN_OUT);
		//?}
		//? if <1.21.2 {
		player.damage(source, Float.MAX_VALUE);
		//?} else {
		/*// >=1.21.2：damage 需要显式传入 ServerWorld；此处仅在服务端调用，直接转换当前世界即可。
		player.damage((ServerWorld) LifeDebtWorldHelper.getWorld(player), source, Float.MAX_VALUE);
		*///?}
	}
}
