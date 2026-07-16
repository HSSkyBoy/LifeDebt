package com.adoleiiiiii.lifedebt.mixin;

import com.adoleiiiiii.lifedebt.damage.LifeDebtDamageTypes;
import com.adoleiiiiii.lifedebt.effect.ModEffects;
import com.adoleiiiiii.lifedebt.player.LifeDebtPlayerAccess;
import com.adoleiiiiii.lifedebt.util.LifeDebtDamageHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
//? if >=1.21.2 {
/*import net.minecraft.server.world.ServerWorld;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 在玩家拥有不屈 buff 且已触发过死亡抵抗时，按公式减免所受伤害。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

	/**
	 * 根据不屈 buff 期间的死亡次数，缩放即将结算的伤害值。
	 */
	//? if <1.21.2 {
	@ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
	private float lifedebt$modifyIncomingDamage(float amount, DamageSource source) {
	//?} else {
	/*// >=1.21.2：damage 签名变为 (ServerWorld, DamageSource, float)，
	// 处理器按「被修改变量 + 目标方法前缀参数」的顺序捕获 ServerWorld 与 DamageSource。
	@ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
	private float lifedebt$modifyIncomingDamage(float amount, ServerWorld world, DamageSource source) {
	*///?}
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof PlayerEntity player)) {
			return amount;
		}
		if (source.isOf(LifeDebtDamageTypes.BURN_OUT)) {
			return amount;
		}
		if (!player.hasStatusEffect(ModEffects.LIFE_DEBT)) {
			return amount;
		}

		LifeDebtPlayerAccess access = (LifeDebtPlayerAccess) player;
		if (access.lifedebt$isEffectEndSettled()) {
			return amount;
		}
		int deathCount = access.lifedebt$getDeathCount();
		if (deathCount <= 0) {
			return amount;
		}

		float multiplier = LifeDebtDamageHelper.computeDamageMultiplier(deathCount, player.getMaxHealth());
		return amount * multiplier;
	}
}
