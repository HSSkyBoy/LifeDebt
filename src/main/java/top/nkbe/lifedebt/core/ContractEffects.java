package top.nkbe.lifedebt.core;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.item.ModItems;

/**
 * 契约的主动玩法效果——把「债」变成对应契约的力量，而不只是惩罚。
 *
 * <p>两类入口：
 * <ul>
 *   <li>{@link #tick}：持续型效果，每若干 tick 依当前状态重算（血契的低血增伤与攻速）。</li>
 *   <li>{@link #onBorrow}：借命瞬间触发的一次性效果（魂契支付代价减损、亡契瞬移无敌）。</li>
 * </ul>
 * 非对应契约的分支自然不触发，切换契约后 tick 型效果会被移除清理。
 */
public final class ContractEffects {

	/** 血契近战增伤修饰符 ID（临时修饰符，随血量每 tick 重算，不持久化）。 */
	private static final Identifier BLOOD_DAMAGE_ID =
			Identifier.of("lifedebt", "blood_pact_damage");

	/** 血契攻速修饰符 ID（与增伤同步随血量重算）。 */
	private static final Identifier BLOOD_ATTACK_SPEED_ID =
			Identifier.of("lifedebt", "blood_pact_attack_speed");

	/** 血契满缺血时的最大增伤比例（1.5 = +150%）。作为主要调参旋钮。 */
	private static final double BLOOD_MAX_DAMAGE_BONUS = 1.5;

	/** 血契满缺血时的最大攻速加成比例（0.6 = +60%）。 */
	private static final double BLOOD_MAX_ATTACK_SPEED_BONUS = 0.6;

	/** 血契借命的额外债务：狂战代价高，「死亡增加大量债务」。 */
	private static final int BLOOD_EXTRA_DEBT = 1;

	/** 魂契借命减损所需的经验等级；付得起才触发。 */
	private static final int SOUL_XP_COST = 3;

	/** 魂契经验不足时，改从护甲抽取的耐久点数。 */
	private static final int SOUL_DURABILITY_COST = 40;

	/** 亡契借命后瞬移的水平距离范围（方块）。 */
	private static final int ESCAPE_MIN_DISTANCE = 6;
	private static final int ESCAPE_MAX_DISTANCE = 10;

	/** 亡契常驻移速修饰符 ID（随债务等级每 tick 重算）。 */
	private static final Identifier ESCAPE_SPEED_ID =
			Identifier.of("lifedebt", "escape_pact_speed");

	/** 亡契常驻移速加成基准（0.10 = +10%），随债务等级放大。 */
	private static final double ESCAPE_SPEED_BONUS = 0.10;

	/** 魂契经验获取加成基准（0.25 = +25%），随债务等级放大。 */
	private static final double SOUL_XP_BONUS = 0.25;

	private ContractEffects() {
	}

	/**
	 * 按当前状态刷新契约效果。应在服务端周期性调用（每玩家）。
	 * 非对应契约时会移除效果，切换契约后自动清理。
	 */
	public static void tick(ServerPlayerEntity player) {
		updateBloodPact(player);
		updateEscapePact(player);
	}

	/**
	 * 负债越深、力量越强：按债务等级放大契约增益。
	 * 正常 1.0，每升一级 +0.2，死人未亡 1.8——玩家可以主动养债换取强度。
	 */
	public static double debtScale(LifeDebtData data) {
		return 1.0 + 0.2 * data.getLevel().ordinal();
	}

	/**
	 * 魂契的经验获取倍率（供 {@code PlayerEntityXpMixin} 调用）；非魂契返回 1。
	 * 基准 +{@value #SOUL_XP_BONUS}，随债务等级放大。
	 */
	public static double xpMultiplier(ServerPlayerEntity player) {
		LifeDebtData data = LifeDebtAttachments.get(player);
		if (data.getContract() != ContractType.SOUL) {
			return 1.0;
		}
		return 1.0 + SOUL_XP_BONUS * debtScale(data);
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

	/** 血契：借命额外累积债务，并短暂获得力量——越战越负，也越战越狂。 */
	private static void onBloodBorrow(ServerPlayerEntity player, LifeDebtData data) {
		data.addDebt(BLOOD_EXTRA_DEBT);
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 1));
	}

	/**
	 * 魂契：借命时支付代价换取更低的生命损失——按「经验 → 债券 → 装备耐久」的顺序递补，
	 * 付得起即回赠护盾与再生并抵销这次借命的债务；三者皆无则如常借命。
	 */
	private static void onSoulBorrow(ServerPlayerEntity player, LifeDebtData data) {
		if (!paySoulCost(player)) {
			player.sendMessage(Text.translatable("lifedebt.message.soul_poor"), true);
			return;
		}
		// 吸收护盾 + 再生：把「更低的生命损失」落成借命瞬间的即时补偿。
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 600, 1));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
		data.setDebt(Math.max(0, data.getDebt() - 1));
	}

	/** 依「经验 → 债券 → 装备耐久」的顺序支付魂契代价；成功支付返回 {@code true}。 */
	private static boolean paySoulCost(ServerPlayerEntity player) {
		if (player.experienceLevel >= SOUL_XP_COST) {
			player.addExperienceLevels(-SOUL_XP_COST);
			player.sendMessage(Text.translatable("lifedebt.message.soul_pay", SOUL_XP_COST), true);
			return true;
		}
		for (int slot = 0; slot < player.getInventory().size(); slot++) {
			ItemStack stack = player.getInventory().getStack(slot);
			if (stack.isOf(ModItems.DEBT_VOUCHER)) {
				stack.decrement(1);
				player.sendMessage(Text.translatable("lifedebt.message.soul_pay_item"), true);
				return true;
			}
		}
		// 耐久只抽取、不打碎：要求剩余耐久高于抽取量，装备不会因魂契直接损毁。
		for (EquipmentSlot slot : new EquipmentSlot[] {
				EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
			ItemStack armor = player.getEquippedStack(slot);
			if (armor.isDamageable() && armor.getMaxDamage() - armor.getDamage() > SOUL_DURABILITY_COST) {
				armor.setDamage(armor.getDamage() + SOUL_DURABILITY_COST);
				player.sendMessage(Text.translatable("lifedebt.message.soul_pay_durability", SOUL_DURABILITY_COST), true);
				return true;
			}
		}
		return false;
	}

	/**
	 * 亡契：借命时瞬移脱身并获得短暂无敌（抗性 V ≈ 免伤），
	 * 附带速度与短暂隐身，帮助玩家真正脱离战场。
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
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 1));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 60, 0));
		player.sendMessage(Text.translatable("lifedebt.message.escape"), true);
	}

	private static void updateBloodPact(ServerPlayerEntity player) {
		EntityAttributeInstance attack = player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
		EntityAttributeInstance attackSpeed = player.getAttributeInstance(EntityAttributes.ATTACK_SPEED);
		if (attack == null || attackSpeed == null) {
			return;
		}

		// 先移除旧值：非血契 / 血量变化都靠「移除后按需重加」保持同步。
		attack.removeModifier(BLOOD_DAMAGE_ID);
		attackSpeed.removeModifier(BLOOD_ATTACK_SPEED_ID);

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
		if (missing <= 0.0) {
			return;
		}

		// ADD_MULTIPLIED_TOTAL：对含武器加成的最终数值整体乘 (1 + bonus)；债务等级放大上限。
		double scale = debtScale(data);
		attack.addTemporaryModifier(new EntityAttributeModifier(
				BLOOD_DAMAGE_ID, missing * BLOOD_MAX_DAMAGE_BONUS * scale,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		attackSpeed.addTemporaryModifier(new EntityAttributeModifier(
				BLOOD_ATTACK_SPEED_ID, missing * BLOOD_MAX_ATTACK_SPEED_BONUS * scale,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}

	/** 亡契常驻收益：移速加成，跑图、探索、脱身都更利落；随债务等级放大。 */
	private static void updateEscapePact(ServerPlayerEntity player) {
		EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}
		speed.removeModifier(ESCAPE_SPEED_ID);
		LifeDebtData data = LifeDebtAttachments.get(player);
		if (data.getContract() != ContractType.ESCAPE) {
			return;
		}
		speed.addTemporaryModifier(new EntityAttributeModifier(
				ESCAPE_SPEED_ID, ESCAPE_SPEED_BONUS * debtScale(data),
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}
}
