package com.adoleiiiiii.lifedebt.effect;

import com.adoleiiiiii.lifedebt.util.LifeDebtPenaltyHandler;
import net.minecraft.entity.LivingEntity;
//? if <1.20.5 {
import net.minecraft.entity.attribute.AttributeContainer;
//?}
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

/**
 * 不屈状态效果：buff 期间抵抗死亡并获得叠乘减伤与击退抗性，结束时按死亡次数扣除生命上限。
 */
public class LifeDebtEffect extends StatusEffect {

	public LifeDebtEffect() {
		super(StatusEffectCategory.BENEFICIAL, 0xFFD700);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}

	//? if <1.20.5 {
	@Override
	public void applyUpdateEffect(LivingEntity entity, int amplifier) {
		// 不屈效果是被动触发的，不需要每 tick 更新
	}
	//?} else {
	/*@Override
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		// 不屈效果是被动触发的，不需要每 tick 更新；返回 false 会导致效果被立即移除，必须返回 true
		return true;
	}
	*///?}

	//? if <1.20.5 {
	@Override
	public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
		super.onApplied(entity, attributes, amplifier);

		if (entity instanceof PlayerEntity player) {
			LifeDebtPenaltyHandler.handleEffectApplied(player);
		}
	}

	@Override
	public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
		super.onRemoved(entity, attributes, amplifier);

		if (entity instanceof PlayerEntity player) {
			LifeDebtPenaltyHandler.handleEffectEnd(player);
		}
	}
	//?} else {
	/*@Override
	public void onApplied(LivingEntity entity, int amplifier) {
		super.onApplied(entity, amplifier);

		if (entity instanceof PlayerEntity player) {
			LifeDebtPenaltyHandler.handleEffectApplied(player);
		}
	}

	// >=1.20.5 的 StatusEffect 不再提供带 LivingEntity 的 onRemoved 钩子（仅剩 onRemoved(AttributeContainer)），
	// 效果结束结算改由 LivingEntityRemoveEffectMixin 注入 LivingEntity#onStatusEffectRemoved 完成。
	*///?}
}
