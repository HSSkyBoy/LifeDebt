package com.adoleiiiiii.lifedebt;

import com.adoleiiiiii.lifedebt.config.LifeDebtConfig;
import com.adoleiiiiii.lifedebt.effect.ModEffects;
import com.adoleiiiiii.lifedebt.mixin.ItemAccessor;
import com.adoleiiiiii.lifedebt.player.LifeDebtPlayerAccess;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class LifeDebt implements ModInitializer {

	public static final String MOD_ID = "lifedebt";

	@Override
	public void onInitialize() {
		LifeDebtConfig.load();
		ModEffects.initialize();

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (newPlayer instanceof LifeDebtPlayerAccess access) {
				access.lifedebt$clearMaxHealthPenalty();
				access.lifedebt$setBuffSessionActive(false);
				access.lifedebt$setEffectEndSettled(false);
			}
		});

		Item totem = Items.TOTEM_OF_UNDYING;
		if (totem instanceof ItemAccessor accessor) {
			accessor.setFoodComponent(ModFoodComponents.TOTEM_OF_UNDYING);
		}
	}
}
