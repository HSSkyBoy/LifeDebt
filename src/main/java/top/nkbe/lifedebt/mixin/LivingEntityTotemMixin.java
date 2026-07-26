package top.nkbe.lifedebt.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.nkbe.lifedebt.core.LifeDebtManager;

/**
 * 签约者手持不死图腾时，借命优先于原版图腾免死——债务照记、图腾保留。
 * 未签约或容量耗尽时不干涉，原版图腾行为照常。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityTotemMixin {

	@Inject(method = "tryUseDeathProtector", at = @At("HEAD"), cancellable = true)
	private void lifedebt$borrowBeforeTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ServerPlayerEntity player && LifeDebtManager.handleDeath(player)) {
			cir.setReturnValue(true);
		}
	}
}
