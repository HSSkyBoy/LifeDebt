package top.nkbe.lifedebt;

import top.nkbe.lifedebt.config.LifeDebtConfig;
import top.nkbe.lifedebt.core.LifeDebtAttachments;
import top.nkbe.lifedebt.effect.ModEffects;
import top.nkbe.lifedebt.event.LifeDebtEvents;
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
		LifeDebtAttachments.initialize();
		LifeDebtNetworking.registerPayloads();
		LifeDebtNetworking.registerServerReceivers();
		LifeDebtEvents.register();

		// 旧版重生清理逻辑，保留以兼容尚未移除的 1.x 效果系统（当前处于休眠状态）。
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (newPlayer instanceof LifeDebtPlayerAccess access) {
				access.lifedebt$clearMaxHealthPenalty();
				access.lifedebt$setBuffSessionActive(false);
				access.lifedebt$setEffectEndSettled(false);
			}
		});
	}
}
