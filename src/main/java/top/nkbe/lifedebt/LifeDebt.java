package top.nkbe.lifedebt;

import top.nkbe.lifedebt.config.LifeDebtConfig;
import top.nkbe.lifedebt.core.LifeDebtAttachments;
import top.nkbe.lifedebt.core.LifeDebtManager;
import top.nkbe.lifedebt.effect.ModEffects;
import top.nkbe.lifedebt.event.LifeDebtEvents;
import top.nkbe.lifedebt.block.ModBlocks;
import top.nkbe.lifedebt.item.ModItems;
import top.nkbe.lifedebt.entity.ModEntities;
import top.nkbe.lifedebt.net.LifeDebtNetworking;
import top.nkbe.lifedebt.player.LifeDebtPlayerAccess;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public class LifeDebt implements ModInitializer {

	public static final String MOD_ID = "lifedebt";

	@Override
	public void onInitialize() {
		LifeDebtConfig.load();
		ModEffects.initialize();
		ModBlocks.initialize();
		ModItems.initialize();
		ModEntities.initialize();
		LifeDebtAttachments.initialize();
		LifeDebtNetworking.registerPayloads();
		LifeDebtNetworking.registerServerReceivers();
		LifeDebtEvents.register();

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			// 旧版重生清理逻辑
			if (newPlayer instanceof LifeDebtPlayerAccess access) {
				access.lifedebt$clearMaxHealthPenalty();
				access.lifedebt$setBuffSessionActive(false);
				access.lifedebt$setEffectEndSettled(false);
			}

			// 「债」跨死亡
			LifeDebtManager.reapplyMaxHealthPenalty(newPlayer);
			LifeDebtManager.updateContractPenalty(newPlayer);
		});
	}
}
