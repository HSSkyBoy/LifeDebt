package top.nkbe.lifedebt.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.LifeDebt;

/**
 * S2C：服务端通知客户端打开契约选择界面。
 * 本身不携带数据——客户端已知全部契约类型，界面内容由客户端决定。
 */
public record OpenContractScreenPayload() implements CustomPayload {

	public static final CustomPayload.Id<OpenContractScreenPayload> ID =
			new CustomPayload.Id<>(Identifier.of(LifeDebt.MOD_ID, "open_contract_screen"));

	public static final PacketCodec<RegistryByteBuf, OpenContractScreenPayload> CODEC =
			PacketCodec.unit(new OpenContractScreenPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
