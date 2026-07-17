package top.nkbe.lifedebt.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import top.nkbe.lifedebt.net.OpenContractScreenPayload;
import top.nkbe.lifedebt.entity.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * 命债客户端入口。只注册客户端专属的表现层逻辑（当前：契约选择界面）。
 * 权威玩法一律在服务端，本类不做任何游戏状态判断。
 */
@Environment(EnvType.CLIENT)
public final class LifeDebtClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.DEBT_COLLECTOR, DebtCollectorRenderer::new);
		ClientPlayNetworking.registerGlobalReceiver(OpenContractScreenPayload.ID,
				(payload, context) -> context.client().execute(
						() -> context.client().setScreen(new ContractSelectScreen())));
		LifeDebtHud.register();
	}
}
