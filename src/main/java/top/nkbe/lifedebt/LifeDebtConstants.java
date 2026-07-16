package top.nkbe.lifedebt;

//? if <1.21 {
import java.util.UUID;
//?} else {
/*import net.minecraft.util.Identifier;
*///?}

/**
 * 不屈效果相关的常量定义。
 */
public final class LifeDebtConstants {

	private LifeDebtConstants() {
	}

	//? if <1.21 {
	/** 效果结束时扣除生命上限所使用的属性修饰符 ID。 */
	public static final UUID MAX_HEALTH_PENALTY_MODIFIER_ID =
			UUID.fromString("a3984b3d-99e1-49b0-b7a6-b5090bc0d6ea");

	/** 不屈 buff 叠乘减伤对应的击退抗性属性修饰符 ID。 */
	public static final UUID KNOCKBACK_RESISTANCE_MODIFIER_ID =
			UUID.fromString("c7e2f1a8-4b3d-4e91-9f62-1d8a6c5b0e47");
	//?} else {
	/*// >=1.21：属性修饰符改用 Identifier 作为 ID。
	public static final Identifier MAX_HEALTH_PENALTY_MODIFIER_ID =
			Identifier.of("lifedebt", "max_health_penalty");

	public static final Identifier KNOCKBACK_RESISTANCE_MODIFIER_ID =
			Identifier.of("lifedebt", "knockback_resistance");
	*///?}
}
