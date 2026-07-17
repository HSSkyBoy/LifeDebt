package top.nkbe.lifedebt.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.LifeDebt;

/**
 * C2S：客户端在契约界面选定后，回传所选契约的字符串 id。
 * 服务端负责校验（是否手持图腾、是否已有容量）与实际签约，客户端不做权威判断。
 */
public record SignContractPayload(String contractId) implements CustomPayload {

	public static final CustomPayload.Id<SignContractPayload> ID =
			new CustomPayload.Id<>(Identifier.of(LifeDebt.MOD_ID, "sign_contract"));

	public static final PacketCodec<RegistryByteBuf, SignContractPayload> CODEC =
			PacketCodec.tuple(PacketCodecs.STRING, SignContractPayload::contractId, SignContractPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
