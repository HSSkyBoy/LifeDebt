package top.nkbe.lifedebt.core;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.LifeDebt;

/**
 * 命债数据的持久化附着注册。使用 Fabric Data Attachment API（fabric-api 内建），
 * 在 1.20.5–1.21.11 全版本段签名一致，无需版本条件编译。
 */
public final class LifeDebtAttachments {

	/**
	 * 玩家命债数据。持久化到存档，并在死亡/重生后保留——债务是这个模组的核心，
	 * 绝不能因为死亡而清零。
	 */
	public static final AttachmentType<LifeDebtData> LIFE_DEBT =
			AttachmentRegistry.<LifeDebtData>builder()
					.persistent(LifeDebtData.CODEC)
					.initializer(LifeDebtData::new)
					.copyOnDeath()
					.buildAndRegister(Identifier.of(LifeDebt.MOD_ID, "data"));

	private LifeDebtAttachments() {
	}

	/** 触发类加载以完成注册。 */
	public static void initialize() {
	}

	/**
	 * 获取玩家的命债数据，若不存在则创建。仅应在服务端调用。
	 */
	public static LifeDebtData get(PlayerEntity player) {
		return player.getAttachedOrCreate(LIFE_DEBT);
	}
}
