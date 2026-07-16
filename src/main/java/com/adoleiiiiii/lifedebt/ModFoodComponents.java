package com.adoleiiiiii.lifedebt;

import com.adoleiiiiii.lifedebt.effect.ModEffects;
import net.minecraft.SharedConstants;
//? if <1.20.5 {
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
//?} else {
/*import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
*///?}

/**
 * 模组内自定义的食物属性定义。
 */
public class ModFoodComponents {

	/** 不死图腾作为食物时的饱食度、饱和度和效果。 */
	public static final FoodComponent TOTEM_OF_UNDYING = new FoodComponent.Builder()
			//? if <1.20.5 {
			.hunger(6)
			//?} else {
			/*.nutrition(6)
			*///?}
			.saturationModifier(1.0f)
			.alwaysEdible()
			.statusEffect(new StatusEffectInstance(ModEffects.LIFE_DEBT, 5 * SharedConstants.TICKS_PER_MINUTE), 1.0f)
			.build();
}
