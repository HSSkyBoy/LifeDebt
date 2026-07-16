package top.nkbe.lifedebt.damage;

//? if <1.19.4 {
/*import net.minecraft.entity.damage.DamageSource;
*///?} else {
import top.nkbe.lifedebt.LifeDebt;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
//?}

/**
 * 模组自定义伤害类型注册键。
 */
public final class LifeDebtDamageTypes {

	/** 不屈 buff 惩罚致死（死亡信息显示为「燃尽了」）。 */
	//? if <1.19.4 {
	/*// <1.19.4：尚无数据驱动的 DamageType 注册表，改用旧式 DamageSource 单例；
	// 死亡信息键由 DamageSource#getDeathMessage 以 "death.attack." + name 拼接，
	// 即沿用 death.attack.burn_out。
	public static final DamageSource BURN_OUT = new DamageSource("burn_out") {};
	*///?} elif <1.21 {
	public static final RegistryKey<DamageType> BURN_OUT =
			RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(LifeDebt.MOD_ID, "burn_out"));
	//?} else {
	/*public static final RegistryKey<DamageType> BURN_OUT =
			RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(LifeDebt.MOD_ID, "burn_out"));
	*///?}

	private LifeDebtDamageTypes() {
	}
}
