package top.nkbe.lifedebt.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.TypedActionResult;
import top.nkbe.lifedebt.core.LifeDebtAttachments;
import top.nkbe.lifedebt.core.LifeDebtData;
import top.nkbe.lifedebt.core.LifeDebtManager;
import top.nkbe.lifedebt.net.OpenContractScreenPayload;

/**
 * 命债玩法的事件接线。集中注册，避免把入口逻辑散落在 {@code onInitialize} 里。
 *
 * <p>本层针对开发主节点 1.21.1 编写；1.21.2+ 的 API 断代（UseItemCallback 返回类型变化）
 * 在补齐现代节点时再用 Stonecutter 条件注释处理。
 */
public final class LifeDebtEvents {

	private LifeDebtEvents() {
	}

	public static void register() {
		registerDeathHook();
		registerTotemSigning();
	}

	/**
	 * 致命伤拦截：使用 Fabric 内建的 ALLOW_DEATH 钩子（专为图腾式取消死亡设计），
	 * 无需 mixin。命债一旦介入借命成功，即返回 false 取消这次死亡。
	 */
	private static void registerDeathHook() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayerEntity player) {
				// handleDeath 返回 true 表示已借命并阻止死亡 → 不允许死亡发生。
				return !LifeDebtManager.handleDeath(player);
			}
			return true;
		});
	}

	/**
	 * 右键不死图腾时，若玩家尚无借命容量，令客户端打开契约选择界面。
	 * 实际签约（写入契约、扣图腾）由 {@link top.nkbe.lifedebt.net.LifeDebtNetworking}
	 * 在收到客户端选择后于服务端权威执行。
	 */
	private static void registerTotemSigning() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (world.isClient() || !stack.isOf(Items.TOTEM_OF_UNDYING)) {
				return TypedActionResult.pass(stack);
			}
			if (!(player instanceof ServerPlayerEntity serverPlayer)) {
				return TypedActionResult.pass(stack);
			}

			LifeDebtData data = LifeDebtAttachments.get(player);
			// 已有剩余借命容量时不重复签约，避免浪费图腾。
			if (data.getTotemCharge() > 0) {
				return TypedActionResult.pass(stack);
			}

			ServerPlayNetworking.send(serverPlayer, new OpenContractScreenPayload());
			return TypedActionResult.success(stack, false);
		});
	}
}
