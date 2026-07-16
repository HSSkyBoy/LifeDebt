package top.nkbe.lifedebt;

import top.nkbe.lifedebt.config.LifeDebtConfig;
import top.nkbe.lifedebt.effect.ModEffects;
import top.nkbe.lifedebt.mixin.ItemAccessor;
import top.nkbe.lifedebt.player.LifeDebtPlayerAccess;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
//? if >=1.20.5 {
/*import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
*///?}
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class LifeDebt implements ModInitializer {

	public static final String MOD_ID = "lifedebt";

	@Override
	public void onInitialize() {
		LifeDebtConfig.load();
		ModEffects.initialize();

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (newPlayer instanceof LifeDebtPlayerAccess) {
				LifeDebtPlayerAccess access = (LifeDebtPlayerAccess) newPlayer;
				access.lifedebt$clearMaxHealthPenalty();
				access.lifedebt$setBuffSessionActive(false);
				access.lifedebt$setEffectEndSettled(false);
			}
		});

		Item totem = Items.TOTEM_OF_UNDYING;
		//? if <1.20.5 {
		if (totem instanceof ItemAccessor) {
			ItemAccessor accessor = (ItemAccessor) totem;
			accessor.setFoodComponent(ModFoodComponents.TOTEM_OF_UNDYING);
		}
		//?} elif <1.21.2 {
		/*// >=1.20.5：食物属性存放在物品的数据组件表中，重建 ComponentMap 并追加 FOOD 组件。
		if (totem instanceof ItemAccessor) {
			ItemAccessor accessor = (ItemAccessor) totem;
			accessor.setComponents(ComponentMap.builder()
					.addAll(totem.getComponents())
					.add(DataComponentTypes.FOOD, ModFoodComponents.TOTEM_OF_UNDYING)
					.build());
		}
		*///?} else {
		/*// >=1.21.2：进食效果拆分至 CONSUMABLE 组件，需同时追加 FOOD 与 CONSUMABLE。
		if (totem instanceof ItemAccessor) {
			ItemAccessor accessor = (ItemAccessor) totem;
			accessor.setComponents(ComponentMap.builder()
					.addAll(totem.getComponents())
					.add(DataComponentTypes.FOOD, ModFoodComponents.TOTEM_OF_UNDYING)
					.add(DataComponentTypes.CONSUMABLE, ModFoodComponents.TOTEM_OF_UNDYING_CONSUMABLE)
					.build());
		}
		*///?}
	}
}
