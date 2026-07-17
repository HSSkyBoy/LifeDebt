package top.nkbe.lifedebt.util;

import top.nkbe.lifedebt.player.LifeDebtPlayerAccess;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
//? if >=1.20.5 {
/*import net.minecraft.registry.entry.RegistryEntry;
*///?}

import java.util.ArrayList;
import java.util.List;

/**
 * 不屈图腾触发时的状态效果处理工具。
 */
public final class LifeDebtEffectHelper {

	private LifeDebtEffectHelper() {
	}

	/**
	 * 仅移除负面状态效果，保留不屈等增益效果。
	 *
	 * @param player 玩家实体
	 */
	public static void clearHarmfulStatusEffects(PlayerEntity player) {
		//? if <1.20.5 {
		List<StatusEffect> toRemove = new ArrayList<>();
		for (StatusEffectInstance instance : player.getStatusEffects()) {
			StatusEffect effect = instance.getEffectType();
			if (effect.getCategory() == StatusEffectCategory.HARMFUL) {
				toRemove.add(effect);
			}
		}
		for (StatusEffect effect : toRemove) {
			player.removeStatusEffect(effect);
		}
		//?} else {
		/*List<RegistryEntry<StatusEffect>> toRemove = new ArrayList<>();
		for (StatusEffectInstance instance : player.getStatusEffects()) {
			RegistryEntry<StatusEffect> effect = instance.getEffectType();
			if (effect.value().getCategory() == StatusEffectCategory.HARMFUL) {
				toRemove.add(effect);
			}
		}
		for (RegistryEntry<StatusEffect> effect : toRemove) {
			player.removeStatusEffect(effect);
		}
		*///?}
	}

	/**
	 * 图腾免死后将生命恢复至当前真实上限（清除 buff 期间不应存在的残留惩罚修饰符）。
	 *
	 * @param player 玩家实体
	 */
	public static void restoreHealthToMax(PlayerEntity player) {
		if (player instanceof LifeDebtPlayerAccess access) {
			access.lifedebt$clearMaxHealthPenalty();
		}
		float maxHealth = player.getMaxHealth();
		player.setHealth(maxHealth);
	}
}
