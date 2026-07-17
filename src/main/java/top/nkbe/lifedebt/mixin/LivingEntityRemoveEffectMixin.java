package top.nkbe.lifedebt.mixin;

import top.nkbe.lifedebt.effect.ModEffects;
import top.nkbe.lifedebt.util.LifeDebtPenaltyHandler;
import net.minecraft.entity.LivingEntity;
//? if <1.20.5 {
import net.minecraft.entity.effect.StatusEffect;
//?} else {
/*import net.minecraft.entity.effect.StatusEffectInstance;
*///?}
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//? if <1.20.5 {
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?} else {
/*import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}
//? if >=1.21.2 {
/*import java.util.Collection;
*///?}

/**
 * 在不屈效果被移除时再次尝试结算惩罚（兜底，防止 onRemoved 未触发）。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityRemoveEffectMixin {

	//? if <1.20.5 {
	@Inject(method = "removeStatusEffect", at = @At("RETURN"))
	private void lifedebt$onRemoveStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() || effect != ModEffects.LIFE_DEBT) {
			return;
		}
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof PlayerEntity player) {
			LifeDebtPenaltyHandler.handleEffectEnd(player);
		}
	}
	//?} elif <1.21.2 {
	/*// >=1.20.5：StatusEffect 不再提供带 LivingEntity 的 onRemoved 钩子，
	// 改为注入 onStatusEffectRemoved（removeStatusEffect、clearStatusEffects 与效果自然到期均会经过此方法），
	// 作为效果结束结算的主路径。
	@Inject(method = "onStatusEffectRemoved", at = @At("TAIL"))
	private void lifedebt$onStatusEffectRemoved(StatusEffectInstance instance, CallbackInfo ci) {
		if (instance.getEffectType() != ModEffects.LIFE_DEBT) {
			return;
		}
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof PlayerEntity player) {
			LifeDebtPenaltyHandler.handleEffectEnd(player);
		}
	}
	*///?} else {
	/*// >=1.21.2：onStatusEffectRemoved 改为批量形式 onStatusEffectsRemoved(Collection)
	// （同一 intermediary method_6129），遍历集合命中不屈效果时结算。
	@Inject(method = "onStatusEffectsRemoved", at = @At("TAIL"))
	private void lifedebt$onStatusEffectsRemoved(Collection<StatusEffectInstance> effects, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof PlayerEntity player)) {
			return;
		}
		for (StatusEffectInstance instance : effects) {
			if (instance.getEffectType() == ModEffects.LIFE_DEBT) {
				LifeDebtPenaltyHandler.handleEffectEnd(player);
				return;
			}
		}
	}
	*///?}
}
