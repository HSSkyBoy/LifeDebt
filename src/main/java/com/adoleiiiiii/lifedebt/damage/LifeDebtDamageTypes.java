package com.adoleiiiiii.lifedebt.damage;

import com.adoleiiiiii.lifedebt.LifeDebt;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * 模组自定义伤害类型注册键。
 */
public final class LifeDebtDamageTypes {

	/** 不屈 buff 惩罚致死（死亡信息显示为「燃尽了」）。 */
	//? if <1.21 {
	public static final RegistryKey<DamageType> BURN_OUT =
			RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(LifeDebt.MOD_ID, "burn_out"));
	//?} else {
	/*public static final RegistryKey<DamageType> BURN_OUT =
			RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(LifeDebt.MOD_ID, "burn_out"));
	*///?}

	private LifeDebtDamageTypes() {
	}
}
