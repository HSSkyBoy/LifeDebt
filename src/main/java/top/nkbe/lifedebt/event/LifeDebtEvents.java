package top.nkbe.lifedebt.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import top.nkbe.lifedebt.core.LifeDebtAttachments;
import top.nkbe.lifedebt.core.LifeDebtData;
import top.nkbe.lifedebt.core.LifeDebtManager;

/**
 * 命债玩法的事件接线。集中注册，避免把入口逻辑散落在 {@code onInitialize} 里。
 *
 * <p>本层针对开发主节点 1.21.1 编写；1.21.2+ 的 API 断代（UseItemCallback 返回类型变化）
 * 在补齐现代节点时再用 Stonecutter 条件注释处理。
 */
public final class LifeDebtEvents {

	/** 普通命债图腾的默认借命容量。 */
	private static final int DEFAULT_TOTEM_CHARGE = 3;

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
	 * 右键不死图腾签订命债：授予借命容量并消耗一枚图腾。
	 * Alpha 阶段直接签约，契约选择 UI 于 Milestone 2 接入。
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

			data.setTotemCharge(DEFAULT_TOTEM_CHARGE);
			if (!player.getAbilities().creativeMode) {
				stack.decrement(1);
			}
			serverPlayer.sendMessage(
					Text.translatable("lifedebt.message.signed", DEFAULT_TOTEM_CHARGE), true);
			return TypedActionResult.success(stack, false);
		});
	}
}
