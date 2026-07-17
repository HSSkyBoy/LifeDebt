package top.nkbe.lifedebt.core;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * 契约的主动玩法效果——把「债」变成对应契约的力量，而不只是惩罚。
 *
 * <p>两类入口：
 * <ul>
 *   <li>{@link #tick}：持续型效果，每若干 tick 依当前状态重算（血契的低血增伤）。</li>
 *   <li>{@link #onBorrow}：借命瞬间触发的一次性效果（魂契付经验减损、亡契瞬移无敌）。</li>
 * </ul>
 * 非对应契约的分支自然不触发，切换契约后 tick 型效果会被移除清理。
 */
public final class ContractEffects {

	/** 血契近战增伤修饰符 ID（临时修饰符，随血量每 tick 重算，不持久化）。 */
	private static final Identifier BLOOD_DAMAGE_ID =
			Identifier.of("lifedebt", "blood_pact_damage");

	/** 血契满缺血时的最大增伤比例（1.5 = +150%）。作为主要调参旋钮。 */
	private static final double BLOOD_MAX_DAMAGE_BONUS = 1.5;

	/** 血契借命的额外债务：狂战代价高，「死亡增加大量债务」。 */
	private static final int BLOOD_EXTRA_DEBT = 2;

	/** 魂契借命减损所需的经验等级；付得起才触发。 */
	private static final int SOUL_XP_COST = 3;

	/** 亡契借命后瞬移的水平距离范围（方块）。 */
	private static final int ESCAPE_MIN_DISTANCE = 6;
	private static final int ESCAPE_MAX_DISTANCE = 10;

	private ContractEffects() {
	}

	/**
	 * 按当前状态刷新契约效果。应在服务端周期性调用（每玩家）。
	 * 非对应契约时会移除效果，切换契约后自动清理。
	 */
	public static void tick(ServerPlayerEntity player) {
		updateBloodPact(player);
	}

	/**
	 * 借命瞬间触发对应契约的一次性效果。由 {@code LifeDebtManager.handleDeath} 在
	 * 已确认借命成功、玩家已回满血后调用。
	 */
	public static void onBorrow(ServerPlayerEntity player, LifeDebtData data) {
		switch (data.getContract()) {
			case BLOOD -> onBloodBorrow(player, data);
			case SOUL -> onSoulBorrow(player, data);
			case ESCAPE -> onEscapeBorrow(player, data);
			default -> {
			}
		}
	}

	/** 血契：借命额外累积债务，越战越负。 */
	private static void onBloodBorrow(ServerPlayerEntity player, LifeDebtData data) {
		data.addDebt(BLOOD_EXTRA_DEBT);
	}

	/**
	 * 魂契：借命时以经验换取更低的生命损失——付得起 {@value #SOUL_XP_COST} 级经验，
	 * 即扣除等级并回赠护盾与再生，抵消这次濒死的代价；付不起则如常借命。
	 */
	private static void onSoulBorrow(ServerPlayerEntity player, LifeDebtData data) {
		if (player.experienceLevel < SOUL_XP_COST) {
			player.sendMessage(Text.translatable("lifedebt.message.soul_poor"), true);
			return;
		}
		player.addExperienceLevels(-SOUL_XP_COST);
		// 吸收护盾 + 再生：把「更低的生命损失」落成借命瞬间的即时补偿。
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 600, 1));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
		player.sendMessage(Text.translatable("lifedebt.message.soul_pay", SOUL_XP_COST), true);
	}

	/**
	 * 亡契：借命时瞬移脱身并获得短暂无敌（抗性 V ≈ 免伤），代价是额外债务——
	 * 逃得掉一时，却把追债者引得更近。
	 */
	private static void onEscapeBorrow(ServerPlayerEntity player, LifeDebtData data) {
		double angle = player.getRandom().nextDouble() * Math.PI * 2.0;
		double distance = ESCAPE_MIN_DISTANCE
				+ player.getRandom().nextDouble() * (ESCAPE_MAX_DISTANCE - ESCAPE_MIN_DISTANCE);
		double x = player.getX() + Math.cos(angle) * distance;
		double z = player.getZ() + Math.sin(angle) * distance;
		player.requestTeleport(x, player.getY(), z);
		// 抗性 V（amplifier 4）在原版为 100% 减伤，兜住落点与追兵的短窗口。
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 4));
		data.addDebt(1);
		player.sendMessage(Text.translatable("lifedebt.message.escape"), true);
	}

	private static void updateBloodPact(ServerPlayerEntity player) {
		EntityAttributeInstance attack = player.getAttributeInstance(
				//? if >=1.21.2 {
				/*EntityAttributes.ATTACK_DAMAGE
				*///?} else {
				EntityAttributes.GENERIC_ATTACK_DAMAGE
				//?}
		);
		if (attack == null) {
			return;
		}

		// 先移除旧值：非血契 / 血量变化都靠「移除后按需重加」保持同步。
		attack.removeModifier(BLOOD_DAMAGE_ID);

		LifeDebtData data = LifeDebtAttachments.get(player);
		if (data.getContract() != ContractType.BLOOD) {
			return;
		}

		float maxHealth = player.getMaxHealth();
		if (maxHealth <= 0.0f) {
			return;
		}

		// 缺失血量比例：满血 0、濒死趋近 1。
		double missing = Math.max(0.0, Math.min(1.0, 1.0 - player.getHealth() / maxHealth));
		double bonus = missing * BLOOD_MAX_DAMAGE_BONUS;
		if (bonus <= 0.0) {
			return;
		}

		// ADD_MULTIPLIED_TOTAL：对含武器加成的最终近战伤害整体乘 (1 + bonus)。
		attack.addTemporaryModifier(new EntityAttributeModifier(
				BLOOD_DAMAGE_ID, bonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}
}
