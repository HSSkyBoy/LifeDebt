package top.nkbe.lifedebt.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.nkbe.lifedebt.core.ContractType;
import top.nkbe.lifedebt.core.LifeDebtAttachments;
import top.nkbe.lifedebt.core.LifeDebtData;
import top.nkbe.lifedebt.core.LifeDebtManager;

/**
 * 命债签约的网络接线：注册双向载荷，并在服务端处理客户端的签约请求。
 *
 * <p>权威判断全部收敛在服务端 {@link #handleSign}，客户端界面只负责收集选择、发包。
 */
public final class LifeDebtNetworking {

	private static final Logger LOGGER = LoggerFactory.getLogger("lifedebt");

	/** 普通命债图腾的默认借命容量。 */
	public static final int DEFAULT_TOTEM_CHARGE = 3;

	private LifeDebtNetworking() {
	}

	/** 注册载荷类型（客户端与服务端都必须调用，故放在通用初始化里）。 */
	public static void registerPayloads() {
		PayloadTypeRegistry.playS2C().register(OpenContractScreenPayload.ID, OpenContractScreenPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(LifeDebtStatePayload.ID, LifeDebtStatePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SignContractPayload.ID, SignContractPayload.CODEC);
	}

	/** 把玩家当前命债状态推给其客户端，供 HUD 显示。 */
	public static void syncState(ServerPlayerEntity player) {
		LifeDebtData data = LifeDebtAttachments.get(player);
		ServerPlayNetworking.send(player, new LifeDebtStatePayload(
				data.getDebt(), data.getTotemCharge(), data.getBorrowedLife(),
				data.getContract().asString()));
	}

	/** 注册服务端接收器：处理客户端提交的签约选择。 */
	public static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(SignContractPayload.ID,
				(payload, context) -> handleSign(context.player(), payload.contractId()));
	}

	/**
	 * 服务端权威签约：校验手持图腾且尚无借命容量后，写入契约与容量并消耗一枚图腾。
	 */
	private static void handleSign(ServerPlayerEntity player, String contractId) {
		LifeDebtData data = LifeDebtAttachments.get(player);
		// 已有剩余容量时不重复签约，避免浪费图腾。
		if (data.getTotemCharge() > 0) {
			return;
		}

		ContractType contract = ContractType.byId(contractId);
		if (contract == ContractType.NONE) {
			return;
		}

		// 定位手持的图腾（主手优先）。客户端不可信，图腾归属由服务端复核。
		Hand hand = null;
		if (player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
			hand = Hand.MAIN_HAND;
		} else if (player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
			hand = Hand.OFF_HAND;
		}
		if (hand == null) {
			return;
		}

		data.setContract(contract);
		data.setTotemCharge(DEFAULT_TOTEM_CHARGE);
		LifeDebtManager.updateContractPenalty(player);
		if (!player.getAbilities().creativeMode) {
			player.getStackInHand(hand).decrement(1);
		}

		LOGGER.info("[签约] {} 签订{}，获得借命容量={}",
				player.getName().getString(), contract.asString(), DEFAULT_TOTEM_CHARGE);
		player.sendMessage(
				Text.translatable("lifedebt.message.signed_contract",
						Text.translatable(contract.translationKey()), DEFAULT_TOTEM_CHARGE),
				true);
	}
}
