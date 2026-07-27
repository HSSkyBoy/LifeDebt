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
 * 玩家受到致命伤时，拦截原版不死图腾保护，统一交由命债系统处理。
 * 有剩余容量时借命成功；容量耗尽或未签约时则正常死亡。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityTotemMixin {

	@Inject(method = "tryUseDeathProtector", at = @At("HEAD"), cancellable = true)
	private void lifedebt$borrowBeforeTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ServerPlayerEntity player) {
			// Never fall through to vanilla Totem of Undying behavior. The return
			// value reflects whether Life Debt successfully prevented the death.
			cir.setReturnValue(LifeDebtManager.handleDeath(player));
		}
	}
}
