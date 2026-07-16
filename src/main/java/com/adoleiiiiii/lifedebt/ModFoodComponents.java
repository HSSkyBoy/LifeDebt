package com.adoleiiiiii.lifedebt;

import com.adoleiiiiii.lifedebt.effect.ModEffects;
import net.minecraft.SharedConstants;
//? if <1.20.5 {
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
//?} elif <1.21.2 {
/*import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
*///?} else {
/*// >=1.21.2：进食效果从 FoodComponent 拆分到 CONSUMABLE 组件（ConsumableComponent + ConsumeEffect）。
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
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
			//? if <1.21.2 {
			.statusEffect(new StatusEffectInstance(ModEffects.LIFE_DEBT, 5 * SharedConstants.TICKS_PER_MINUTE), 1.0f)
			//?}
			.build();

	//? if >=1.21.2 {
	/*// >=1.21.2：FoodComponent.Builder 不再支持 statusEffect，进食效果改由 CONSUMABLE 组件承载；
	// ConsumableComponents.food() 提供与普通食物一致的进食时长、音效与粒子。
	public static final ConsumableComponent TOTEM_OF_UNDYING_CONSUMABLE = ConsumableComponents.food()
			.consumeEffect(new ApplyEffectsConsumeEffect(
					new StatusEffectInstance(ModEffects.LIFE_DEBT, 5 * SharedConstants.TICKS_PER_MINUTE)))
			.build();
	*///?}
}
