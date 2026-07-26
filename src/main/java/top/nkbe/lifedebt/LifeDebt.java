package top.nkbe.lifedebt;

import top.nkbe.lifedebt.core.LifeDebtAttachments;
import top.nkbe.lifedebt.core.LifeDebtManager;
import top.nkbe.lifedebt.event.LifeDebtEvents;
import top.nkbe.lifedebt.block.ModBlocks;
import top.nkbe.lifedebt.item.ModItems;
import top.nkbe.lifedebt.entity.ModEntities;
import top.nkbe.lifedebt.command.LifeDebtCommands;
import top.nkbe.lifedebt.net.LifeDebtNetworking;
import top.nkbe.lifedebt.worldgen.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public class LifeDebt implements ModInitializer {

	public static final String MOD_ID = "lifedebt";

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModItems.initialize();
		ModEntities.initialize();
		ModWorldGen.initialize();
		LifeDebtAttachments.initialize();
		LifeDebtNetworking.registerPayloads();
		LifeDebtNetworking.registerServerReceivers();
		LifeDebtEvents.register();
		LifeDebtCommands.register();

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			LifeDebtManager.reapplyMaxHealthPenalty(newPlayer);
			LifeDebtManager.updateContractPenalty(newPlayer);
		});
	}
}
