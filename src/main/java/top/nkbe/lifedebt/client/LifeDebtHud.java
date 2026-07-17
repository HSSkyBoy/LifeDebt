package top.nkbe.lifedebt.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import top.nkbe.lifedebt.core.ContractType;
import top.nkbe.lifedebt.core.DebtLevel;
import top.nkbe.lifedebt.net.LifeDebtStatePayload;

/**
 * 债务 / 借命 HUD：左上角显示当前契约、借命容量与债务等级。
 *
 * <p>只负责表现——数据来自服务端 {@link LifeDebtStatePayload} 的只读快照，
 * 客户端不做任何权威判断。无契约且无债务时不显示，避免干扰普通玩家。
 */
@Environment(EnvType.CLIENT)
public final class LifeDebtHud {

	private static final int MARGIN = 4;
	private static final int LINE_HEIGHT = 10;
	private static final int TEXT_COLOR = 0xFFFFFF;

	// 最近一次服务端快照。默认无契约 → 不渲染。
	private static volatile int debt;
	private static volatile int totemCharge;
	private static volatile int borrowedLife;
	private static volatile ContractType contract = ContractType.NONE;

	private LifeDebtHud() {
	}

	/** 注册客户端接收器与 HUD 渲染回调。由 {@code LifeDebtClient} 调用。 */
	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(LifeDebtStatePayload.ID,
				(payload, context) -> context.client().execute(() -> {
					debt = payload.debt();
					totemCharge = payload.totemCharge();
					borrowedLife = payload.borrowedLife();
					contract = ContractType.byId(payload.contractId());
				}));
		HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
	}

	private static void render(DrawContext context) {
		// 未签约且无欠债 → 玩家还没入局，不打扰。
		if (contract == ContractType.NONE && debt <= 0 && borrowedLife <= 0) {
			return;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) {
			return;
		}
		TextRenderer font = client.textRenderer;

		int y = MARGIN;
		if (contract != ContractType.NONE) {
			context.drawTextWithShadow(font, Text.translatable(contract.translationKey()),
					MARGIN, y, TEXT_COLOR);
			y += LINE_HEIGHT;
		}
		context.drawTextWithShadow(font,
				Text.translatable("lifedebt.hud.charge", totemCharge), MARGIN, y, TEXT_COLOR);
		y += LINE_HEIGHT;

		DebtLevel level = DebtLevel.fromDebt(debt);
		context.drawTextWithShadow(font,
				Text.translatable("lifedebt.hud.debt", debt, Text.translatable(level.translationKey())),
				MARGIN, y, TEXT_COLOR);
	}
}
