package top.nkbe.lifedebt.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.LifeDebt;

/**
 * S2C：把玩家的命债状态同步给客户端，供 HUD 显示。
 * 权威状态始终在服务端，本包只是只读快照，客户端不据此做任何判断。
 */
public record LifeDebtStatePayload(int debt, int totemCharge, int borrowedLife, String contractId)
		implements CustomPayload {

	public static final CustomPayload.Id<LifeDebtStatePayload> ID =
			new CustomPayload.Id<>(Identifier.of(LifeDebt.MOD_ID, "state"));

	public static final PacketCodec<RegistryByteBuf, LifeDebtStatePayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, LifeDebtStatePayload::debt,
			PacketCodecs.VAR_INT, LifeDebtStatePayload::totemCharge,
			PacketCodecs.VAR_INT, LifeDebtStatePayload::borrowedLife,
			PacketCodecs.STRING, LifeDebtStatePayload::contractId,
			LifeDebtStatePayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
