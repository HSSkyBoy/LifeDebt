package com.adoleiiiiii.lifedebt.effect;

import com.adoleiiiiii.lifedebt.LifeDebt;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
//? if >=1.20.5 {
/*import net.minecraft.registry.entry.RegistryEntry;
*///?}
import net.minecraft.util.Identifier;

/**
 * 模组状态效果注册入口。
 */
public class ModEffects {

	/** 不屈 buff。 */
	//? if <1.20.5 {
	public static final StatusEffect LIFE_DEBT = new LifeDebtEffect();
	//?} else {
	/*// >=1.20.5：StatusEffectInstance/hasStatusEffect 等 API 改用 RegistryEntry<StatusEffect>，
	// 因此注册时保留 registerReference 返回的注册表引用。
	public static final RegistryEntry<StatusEffect> LIFE_DEBT = Registry.registerReference(
			Registries.STATUS_EFFECT, Identifier.of(LifeDebt.MOD_ID, "life_debt"), new LifeDebtEffect());
	*///?}

	private ModEffects() {
	}

	/**
	 * 向游戏注册不屈状态效果。
	 */
	public static void initialize() {
		//? if <1.20.5 {
		Registry.register(Registries.STATUS_EFFECT, new Identifier(LifeDebt.MOD_ID, "life_debt"), LIFE_DEBT);
		//?} else {
		/*// >=1.20.5：注册已在 LIFE_DEBT 字段初始化时通过 Registry.registerReference 完成，
		// 调用本方法触发类加载即可。
		*///?}
	}
}
