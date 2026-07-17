package top.nkbe.lifedebt.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import top.nkbe.lifedebt.core.ContractType;
import top.nkbe.lifedebt.net.SignContractPayload;

/**
 * 契约选择界面：右键图腾签约时由服务端触发打开。
 * 玩家选定后仅发送 C2S 请求，签约成败由服务端裁决。
 */
@Environment(EnvType.CLIENT)
public final class ContractSelectScreen extends Screen {

	/** 可选契约（不含 NONE）。 */
	private static final ContractType[] CHOICES = {
			ContractType.BLOOD, ContractType.SOUL, ContractType.ESCAPE
	};

	private static final int BUTTON_WIDTH = 160;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_GAP = 4;

	public ContractSelectScreen() {
		super(Text.translatable("lifedebt.screen.contract.title"));
	}

	@Override
	protected void init() {
		int totalHeight = CHOICES.length * BUTTON_HEIGHT + (CHOICES.length - 1) * BUTTON_GAP;
		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int y = this.height / 2 - totalHeight / 2;

		for (ContractType contract : CHOICES) {
			addDrawableChild(ButtonWidget.builder(
							Text.translatable(contract.translationKey()),
							button -> choose(contract))
					.dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
					.build());
			y += BUTTON_HEIGHT + BUTTON_GAP;
		}
	}

	private void choose(ContractType contract) {
		ClientPlayNetworking.send(new SignContractPayload(contract.asString()));
		this.close();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title,
				this.width / 2, this.height / 2 - 40, 0xFFFFFF);
	}
}
