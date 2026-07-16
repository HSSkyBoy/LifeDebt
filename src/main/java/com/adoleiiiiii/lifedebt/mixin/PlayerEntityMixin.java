package com.adoleiiiiii.lifedebt.mixin;

import com.adoleiiiiii.lifedebt.effect.ModEffects;
import com.adoleiiiiii.lifedebt.player.LifeDebtPlayerAccess;
import com.adoleiiiiii.lifedebt.util.LifeDebtEffectHelper;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.SharedConstants.TICKS_PER_MINUTE;
import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

/**
 * 拦截图腾免死逻辑：拥有不屈 buff 时触发与图腾等效的免死效果。
 */
@Mixin(LivingEntity.class)
public class PlayerEntityMixin {

	//? if <1.21.2 {
	@Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
	//?} else {
	/*// >=1.21.2：tryUseTotem 更名为 tryUseDeathProtector（签名不变，仍为 (DamageSource)Z）。
	@Inject(method = "tryUseDeathProtector", at = @At("HEAD"), cancellable = true)
	*///?}
	private void onTryUseTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity entity = (LivingEntity) (Object) this;

		if (!(entity instanceof PlayerEntity player)) {
			return;
		}

		if (player.hasStatusEffect(ModEffects.LIFE_DEBT)) {
			if (player instanceof LifeDebtPlayerAccess access) {
				// 效果结束结算或 buff 刷新过程中不应再次触发免死，避免与 removeAllEffects 等批量移除冲突
				if (access.lifedebt$isEffectEndSettled() || access.lifedebt$isRefreshingBuff()) {
					return;
				}
			}
			//? if <1.18 {
			/*// <1.18：Entity 尚无 getWorld()，使用 getEntityWorld()。
			if (!player.getEntityWorld().isClient) {
			*///?} else {
			if (!player.getWorld().isClient) {
			//?}
				triggerLifeDebtEffect(player);
			}
			cir.setReturnValue(true);
			cir.cancel();
		}
	}

	@Unique
	private void triggerLifeDebtEffect(PlayerEntity player) {
		LifeDebtPlayerAccess access = (LifeDebtPlayerAccess) player;
		access.lifedebt$incrementDeathCount();

		StatusEffectInstance lifedebtEffect = player.getStatusEffect(ModEffects.LIFE_DEBT);
		int remainingDuration = lifedebtEffect != null ? lifedebtEffect.getDuration() : -1;

		StatusEffectInstance strengthEffect = player.getStatusEffect(StatusEffects.STRENGTH);
		int strengthAmplifier = strengthEffect != null ? strengthEffect.getAmplifier() : -1;

		access.lifedebt$setRefreshingBuff(true);
		try {
			LifeDebtEffectHelper.clearHarmfulStatusEffects(player);
		} finally {
			access.lifedebt$setRefreshingBuff(false);
		}

		if (!player.hasStatusEffect(ModEffects.LIFE_DEBT)) {
			player.addStatusEffect(new StatusEffectInstance(
					ModEffects.LIFE_DEBT, remainingDuration, 0, false, false, true));
		}

		player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 45 * TICKS_PER_SECOND, 1));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 5 * TICKS_PER_SECOND, 1));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40 * TICKS_PER_SECOND, 0));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 3 * TICKS_PER_MINUTE, 0));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, TICKS_PER_MINUTE, strengthAmplifier + 2));

		LifeDebtEffectHelper.restoreHealthToMax(player);

		//? if <1.18 {
		/*World world = player.getEntityWorld();
		*///?} else {
		World world = player.getWorld();
		//?}
		world.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);

		player.incrementStat(Stats.USED.getOrCreateStat(Items.TOTEM_OF_UNDYING));

		if (player instanceof ServerPlayerEntity serverPlayer) {
			Criteria.USED_TOTEM.trigger(serverPlayer, new ItemStack(Items.TOTEM_OF_UNDYING));
		}

		if (world instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(
					net.minecraft.particle.ParticleTypes.TOTEM_OF_UNDYING,
					player.getX(), player.getY() + player.getHeight() / 2.0, player.getZ(),
					100, 0.5, 0.5, 0.5, 0.5
			);
		}
	}
}
