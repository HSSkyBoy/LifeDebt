package com.adoleiiiiii.lifedebt.effect;

import com.adoleiiiiii.lifedebt.LifeDebt;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 模组状态效果注册入口。
 */
public class ModEffects {

	/** 不屈 buff。 */
	public static final StatusEffect LIFE_DEBT = new LifeDebtEffect();

	private ModEffects() {
	}

	/**
	 * 向游戏注册不屈状态效果。
	 */
	public static void initialize() {
		Registry.register(Registries.STATUS_EFFECT, new Identifier(LifeDebt.MOD_ID, "life_debt"), LIFE_DEBT);
	}
}
