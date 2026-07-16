package com.adoleiiiiii.lifedebt.mixin;

import com.adoleiiiiii.lifedebt.LifeDebtConstants;
import com.adoleiiiiii.lifedebt.effect.ModEffects;
import com.adoleiiiiii.lifedebt.player.LifeDebtPlayerAccess;
import com.adoleiiiiii.lifedebt.player.LifeDebtPlayerNbt;
import com.adoleiiiiii.lifedebt.util.LifeDebtDamageHelper;
import com.adoleiiiiii.lifedebt.util.LifeDebtPenaltyHandler;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为玩家实体附加不屈 buff 期间的死亡计数与生命上限惩罚状态。
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityLifeDebtMixin implements LifeDebtPlayerAccess {

	@Unique
	private int lifedebt$deathCount;

	@Unique
	private boolean lifedebt$refreshingBuff;

	@Unique
	private boolean lifedebt$buffSessionActive;

	@Unique
	private boolean lifedebt$effectEndSettled;

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void lifedebt$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		NbtCompound data = new NbtCompound();
		data.putInt(LifeDebtPlayerNbt.DEATH_COUNT, lifedebt$deathCount);
		data.putBoolean(LifeDebtPlayerNbt.BUFF_SESSION_ACTIVE, lifedebt$buffSessionActive);
		data.putBoolean(LifeDebtPlayerNbt.EFFECT_END_SETTLED, lifedebt$effectEndSettled);
		nbt.put(LifeDebtPlayerNbt.ROOT, data);
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void lifedebt$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		PlayerEntity player = (PlayerEntity) (Object) this;
		lifedebt$refreshingBuff = false;

		if (!nbt.contains(LifeDebtPlayerNbt.ROOT)) {
			if (!player.getWorld().isClient) {
				lifedebt$clearSessionState();
			}
			return;
		}

		NbtCompound data = nbt.getCompound(LifeDebtPlayerNbt.ROOT);
		lifedebt$deathCount = Math.max(0, data.getInt(LifeDebtPlayerNbt.DEATH_COUNT));
		lifedebt$buffSessionActive = data.getBoolean(LifeDebtPlayerNbt.BUFF_SESSION_ACTIVE);
		lifedebt$effectEndSettled = data.getBoolean(LifeDebtPlayerNbt.EFFECT_END_SETTLED);

		if (player.getWorld().isClient) {
			return;
		}

		if (player.hasStatusEffect(ModEffects.LIFE_DEBT)) {
			lifedebt$syncKnockbackResistance();
			return;
		}

		if (lifedebt$buffSessionActive && !lifedebt$effectEndSettled) {
			LifeDebtPenaltyHandler.handleEffectEnd(player);
		}

		if (!player.hasStatusEffect(ModEffects.LIFE_DEBT)) {
			lifedebt$clearSessionState();
		}
	}

	@Unique
	private void lifedebt$clearSessionState() {
		lifedebt$deathCount = 0;
		lifedebt$buffSessionActive = false;
		lifedebt$effectEndSettled = false;
		lifedebt$refreshingBuff = false;
		lifedebt$clearKnockbackResistance();
	}

	@Override
	public int lifedebt$getDeathCount() {
		return lifedebt$deathCount;
	}

	@Override
	public void lifedebt$setDeathCount(int deathCount) {
		lifedebt$deathCount = Math.max(0, deathCount);
		lifedebt$syncKnockbackResistance();
	}

	@Override
	public void lifedebt$incrementDeathCount() {
		lifedebt$deathCount++;
		lifedebt$syncKnockbackResistance();
	}

	@Override
	public boolean lifedebt$isRefreshingBuff() {
		return lifedebt$refreshingBuff;
	}

	@Override
	public void lifedebt$setRefreshingBuff(boolean refreshing) {
		lifedebt$refreshingBuff = refreshing;
	}

	@Override
	public boolean lifedebt$isBuffSessionActive() {
		return lifedebt$buffSessionActive;
	}

	@Override
	public void lifedebt$setBuffSessionActive(boolean active) {
		lifedebt$buffSessionActive = active;
	}

	@Override
	public boolean lifedebt$isEffectEndSettled() {
		return lifedebt$effectEndSettled;
	}

	@Override
	public void lifedebt$setEffectEndSettled(boolean settled) {
		lifedebt$effectEndSettled = settled;
	}

	@Override
	public void lifedebt$applyMaxHealthPenalty(float penalty) {
		if (penalty <= 0.0f) {
			return;
		}
		PlayerEntity player = (PlayerEntity) (Object) this;
		EntityAttributeInstance maxHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (maxHealth == null) {
			return;
		}
		lifedebt$clearMaxHealthPenalty();
		//? if <1.20.5 {
		maxHealth.addPersistentModifier(new EntityAttributeModifier(
				LifeDebtConstants.MAX_HEALTH_PENALTY_MODIFIER_ID,
				"lifedebt_max_health_penalty",
				-penalty,
				EntityAttributeModifier.Operation.ADDITION
		));
		//?} elif <1.21 {
		/*maxHealth.addPersistentModifier(new EntityAttributeModifier(
				LifeDebtConstants.MAX_HEALTH_PENALTY_MODIFIER_ID,
				"lifedebt_max_health_penalty",
				-penalty,
				EntityAttributeModifier.Operation.ADD_VALUE
		));
		*///?} else {
		/*maxHealth.addPersistentModifier(new EntityAttributeModifier(
				LifeDebtConstants.MAX_HEALTH_PENALTY_MODIFIER_ID,
				-penalty,
				EntityAttributeModifier.Operation.ADD_VALUE
		));
		*///?}
	}

	@Override
	public void lifedebt$clearMaxHealthPenalty() {
		PlayerEntity player = (PlayerEntity) (Object) this;
		EntityAttributeInstance maxHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (maxHealth != null) {
			maxHealth.removeModifier(LifeDebtConstants.MAX_HEALTH_PENALTY_MODIFIER_ID);
		}
	}

	/**
	 * 按当前死亡次数与最大生命值，同步不屈 buff 对应的击退抗性修饰符。
	 */
	@Unique
	private void lifedebt$syncKnockbackResistance() {
		PlayerEntity player = (PlayerEntity) (Object) this;
		EntityAttributeInstance knockbackResistance =
				player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
		if (knockbackResistance == null) {
			return;
		}

		knockbackResistance.removeModifier(LifeDebtConstants.KNOCKBACK_RESISTANCE_MODIFIER_ID);
		if (!player.hasStatusEffect(ModEffects.LIFE_DEBT) || lifedebt$deathCount <= 0) {
			return;
		}

		float resistance = LifeDebtDamageHelper.computeKnockbackResistance(
				lifedebt$deathCount, player.getMaxHealth());
		if (resistance <= 0.0f) {
			return;
		}

		//? if <1.20.5 {
		knockbackResistance.addTemporaryModifier(new EntityAttributeModifier(
				LifeDebtConstants.KNOCKBACK_RESISTANCE_MODIFIER_ID,
				"lifedebt_knockback_resistance",
				resistance,
				EntityAttributeModifier.Operation.ADDITION
		));
		//?} elif <1.21 {
		/*knockbackResistance.addTemporaryModifier(new EntityAttributeModifier(
				LifeDebtConstants.KNOCKBACK_RESISTANCE_MODIFIER_ID,
				"lifedebt_knockback_resistance",
				resistance,
				EntityAttributeModifier.Operation.ADD_VALUE
		));
		*///?} else {
		/*knockbackResistance.addTemporaryModifier(new EntityAttributeModifier(
				LifeDebtConstants.KNOCKBACK_RESISTANCE_MODIFIER_ID,
				resistance,
				EntityAttributeModifier.Operation.ADD_VALUE
		));
		*///?}
	}

	/**
	 * 移除不屈 buff 附加的击退抗性修饰符。
	 */
	@Unique
	private void lifedebt$clearKnockbackResistance() {
		PlayerEntity player = (PlayerEntity) (Object) this;
		EntityAttributeInstance knockbackResistance =
				player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
		if (knockbackResistance != null) {
			knockbackResistance.removeModifier(LifeDebtConstants.KNOCKBACK_RESISTANCE_MODIFIER_ID);
		}
	}
}
