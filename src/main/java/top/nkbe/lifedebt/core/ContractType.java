package top.nkbe.lifedebt.core;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

/**
 * 契约类型。契约不是 Buff，而是玩家的玩法方向。
 *
 * <p>第一批实现三种契约：血契（狂战）、魂契（法师）、亡契（逃亡）。
 * NONE 表示尚未签订任何契约。
 */
public enum ContractType implements StringIdentifiable {

	/** 未签订契约。 */
	NONE("none"),

	/** 血契：生命越低，伤害与攻速越高，死亡增加大量债务。 */
	BLOOD("blood"),

	/** 魂契：死亡时支付经验/物品/耐久，降低生命损失。 */
	SOUL("soul"),

	/** 亡契：死亡时瞬移并短暂无敌，但增加追债概率。 */
	ESCAPE("escape");

	public static final Codec<ContractType> CODEC = StringIdentifiable.createCodec(ContractType::values);

	private final String id;

	ContractType(String id) {
		this.id = id;
	}

	@Override
	public String asString() {
		return id;
	}
}
