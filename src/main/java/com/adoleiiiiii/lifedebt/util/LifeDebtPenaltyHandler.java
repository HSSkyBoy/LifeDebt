package com.adoleiiiiii.lifedebt.util;

import com.adoleiiiiii.lifedebt.advancement.ModAdvancements;
import com.adoleiiiiii.lifedebt.damage.LifeDebtDamageTypes;
import com.adoleiiiiii.lifedebt.player.LifeDebtPlayerAccess;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

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
		if (player.getWorld().isClient || !(player instanceof LifeDebtPlayerAccess access)) {
			return;
		}

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
		if (player.getWorld().isClient) {
			return;
		}

		if (!(player instanceof LifeDebtPlayerAccess access)) {
			return;
		}

		if (access.lifedebt$isEffectEndSettled() || access.lifedebt$isRefreshingBuff()) {
			return;
		}

		int deathCount = access.lifedebt$getDeathCount();

		access.lifedebt$clearMaxHealthPenalty();
		EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		double baseMax = maxHealthAttr != null ? maxHealthAttr.getBaseValue() : player.getMaxHealth();

		access.lifedebt$setEffectEndSettled(true);
		access.lifedebt$setBuffSessionActive(false);
		access.lifedebt$setDeathCount(0);

		if (deathCount <= 0) {
			return;
		}

		if (baseMax - deathCount <= 0.0) {
			access.lifedebt$applyMaxHealthPenalty(deathCount);
			if (player instanceof ServerPlayerEntity serverPlayer) {
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
		DamageSource source = player.getDamageSources().create(LifeDebtDamageTypes.BURN_OUT);
		player.damage(source, Float.MAX_VALUE);
	}
}
