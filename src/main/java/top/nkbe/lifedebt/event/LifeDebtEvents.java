package top.nkbe.lifedebt.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import top.nkbe.lifedebt.block.ModBlocks;
import top.nkbe.lifedebt.core.ContractEffects;
import top.nkbe.lifedebt.core.LifeDebtAttachments;
import top.nkbe.lifedebt.core.LifeDebtData;
import top.nkbe.lifedebt.core.LifeDebtManager;
import top.nkbe.lifedebt.core.DebtLevel;
import top.nkbe.lifedebt.core.ContractType;
import top.nkbe.lifedebt.item.ModItems;
import top.nkbe.lifedebt.entity.DebtCollectorEntity;
import top.nkbe.lifedebt.entity.ModEntities;

public final class LifeDebtEvents {

	private LifeDebtEvents() {
	}

	public static void register() {
		registerDeathHook();
		registerAltarRepay();
		registerDebtCollectorSpawns();
		registerStarterKit();
		registerContractEffects();
		registerDebtLevelPenalties();
		registerHudSync();
	}

	/**
	 * 周期性把命债状态同步给各在线玩家，驱动客户端 HUD。
	 * 每 20 tick（约 1 秒）一次——HUD 只需大致实时，签约/借命的即时反馈另有 actionbar。
	 */
	private static void registerHudSync() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 != 0) {
				return;
			}
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				top.nkbe.lifedebt.net.LifeDebtNetworking.syncState(player);
			}
		});
	}

	/**
	 * 契约效果周期刷新：血契等玩法效果依当前状态（血量/契约）实时重算。
	 * 每 5 tick 刷新一次，兼顾响应与开销。
	 */
	private static void registerContractEffects() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 5 != 0) {
				return;
			}
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				ContractEffects.tick(player);
			}
		});
	}

	/** Keeps escalating debt consequences active while the player remains in that debt tier. */
	private static void registerDebtLevelPenalties() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 40 != 0) {
				return;
			}
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				DebtLevel level = LifeDebtAttachments.get(player).getLevel();
				if (level.ordinal() >= DebtLevel.FUGITIVE.ordinal()) {
					// Villages turn hostile; every thirty seconds the debt also seizes supplies.
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, 600, 0, false, false, true));
					if (server.getTicks() % 600 == 0) {
						int seizedLevels = Math.min(2, player.experienceLevel);
						if (seizedLevels > 0) {
							player.addExperienceLevels(-seizedLevels);
						}
						player.getHungerManager().setFoodLevel(Math.max(0, player.getHungerManager().getFoodLevel() - 4));
						player.sendMessage(Text.translatable("lifedebt.message.assets_seized", seizedLevels), true);
					}
					// 与没收错峰：亡命者的债务传遍村庄，村民以负面流言抬价（原版声誉机制，还清后随时间衰减恢复）。
					if (server.getTicks() % 600 == 240) {
						spreadDebtGossip(player);
					}
				}
				if (level == DebtLevel.DEAD_NOT_GONE) {
					// Terminal debt must be frightening but recoverable: pressure, never a death loop.
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0, false, false, false));
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1, false, false, false));
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0, false, false, false));
				}
			}
		});
	}

	/**
	 * 村民涨价：对附近村民散布关于该玩家的负面流言。原版交易会按声誉抬高价格
	 * （负声誉 → special price 上调），无需 mixin；流言随时间自然衰减，
	 * 还清债务后价格会在数个游戏日内恢复。
	 */
	private static void spreadDebtGossip(ServerPlayerEntity player) {
		java.util.List<net.minecraft.entity.passive.VillagerEntity> villagers =
				player.getServerWorld().getEntitiesByClass(net.minecraft.entity.passive.VillagerEntity.class,
						player.getBoundingBox().expand(16.0), net.minecraft.entity.passive.VillagerEntity::isAlive);
		for (net.minecraft.entity.passive.VillagerEntity villager : villagers) {
			villager.getGossip().startGossip(player.getUuid(),
					net.minecraft.village.VillageGossipType.MINOR_NEGATIVE, 25);
		}
		if (!villagers.isEmpty()) {
			player.sendMessage(Text.translatable("lifedebt.message.villager_price"), true);
		}
	}

	/**
	 * 致命伤拦截：使用 Fabric 内建的 ALLOW_DEATH 钩子
	 */
	private static void registerDeathHook() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayerEntity player) {
				// handleDeath 返回 true 表示已借命并阻止死亡 → 不允许死亡发生。
				return !LifeDebtManager.handleDeath(player);
			}
			return true;
		});
	}

	private static void registerAltarRepay() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (hand != Hand.MAIN_HAND || world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)
					|| !world.getBlockState(hitResult.getBlockPos()).isOf(ModBlocks.DEBT_ALTAR)) {
				return ActionResult.PASS;
			}

			LifeDebtData data = LifeDebtAttachments.get(serverPlayer);
			if (data.getDebt() <= 0) {
				serverPlayer.sendMessage(net.minecraft.text.Text.translatable("lifedebt.message.no_debt"), true);
				return ActionResult.SUCCESS;
			}

			ItemStack payment = ItemStack.EMPTY;
			if (serverPlayer.getStackInHand(Hand.MAIN_HAND).isOf(ModItems.DEBT_VOUCHER)) {
				payment = serverPlayer.getStackInHand(Hand.MAIN_HAND);
			} else if (serverPlayer.getStackInHand(Hand.OFF_HAND).isOf(ModItems.DEBT_VOUCHER)) {
				payment = serverPlayer.getStackInHand(Hand.OFF_HAND);
			}

			if (!payment.isEmpty()) {
				payment.decrement(1);
				LifeDebtManager.repayOnce(serverPlayer);
				return ActionResult.SUCCESS;
			}

			Hand totemHand = null;
			if (serverPlayer.getStackInHand(Hand.MAIN_HAND).isOf(Items.TOTEM_OF_UNDYING)) {
				totemHand = Hand.MAIN_HAND;
			} else if (serverPlayer.getStackInHand(Hand.OFF_HAND).isOf(Items.TOTEM_OF_UNDYING)) {
				totemHand = Hand.OFF_HAND;
			}
			int cost = data.getLevel().threshold;
			if (totemHand == null) {
				serverPlayer.sendMessage(net.minecraft.text.Text.translatable("lifedebt.message.repay_hint"), true);
				return ActionResult.SUCCESS;
			}
			if (serverPlayer.experienceLevel < cost) {
				serverPlayer.sendMessage(net.minecraft.text.Text.translatable("lifedebt.message.repay_need_xp", cost), true);
				return ActionResult.SUCCESS;
			}
			serverPlayer.addExperienceLevels(-cost);
			serverPlayer.getStackInHand(totemHand).decrement(1);
			LifeDebtManager.repayOnce(serverPlayer);
			return ActionResult.SUCCESS;
		});
	}

	private static void registerDebtCollectorSpawns() {
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (!world.isNight()) {
				return;
			}
			for (ServerPlayerEntity player : world.getPlayers()) {
				LifeDebtData data = LifeDebtAttachments.get(player);
				int debt = data.getDebt();
				if (debt >= DebtLevel.BORROWER.threshold && world.getTime() % 20L == 0L) {
					world.spawnParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(),
							2, 0.25, 0.5, 0.25, 0.01);
				}
				DebtLevel level = DebtLevel.fromDebt(debt);
				int spawnInterval = level == DebtLevel.DEAD_NOT_GONE ? 80
						: level == DebtLevel.FUGITIVE ? 400 : 900;
				if (data.getContract() == ContractType.ESCAPE) {
					// 亡契的代价：逃得掉一时，被追债的概率翻倍。
					spawnInterval = Math.max(40, spawnInterval / 2);
				}
				int nearbyLimit = level == DebtLevel.DEAD_NOT_GONE ? 6
						: level == DebtLevel.FUGITIVE ? 4 : 2;
				if (debt < DebtLevel.DEBTOR.threshold || world.getRandom().nextInt(spawnInterval) != 0) {
					continue;
				}
				Box nearby = player.getBoundingBox().expand(32.0);
				if (world.getEntitiesByClass(DebtCollectorEntity.class, nearby, entity -> true).size() >= nearbyLimit) {
					continue;
				}
				int x = player.getBlockPos().getX() + world.getRandom().nextInt(17) - 8;
				int z = player.getBlockPos().getZ() + world.getRandom().nextInt(17) - 8;
				int y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z) + 4;
				BlockPos spawnPos = new BlockPos(x, y, z);
				if (!world.getBlockState(spawnPos).isAir() || !world.getBlockState(spawnPos.up()).isAir()) {
					continue;
				}
				DebtCollectorEntity collector = ModEntities.DEBT_COLLECTOR.create(world);
				if (collector != null) {
					collector.refreshPositionAndAngles(x + 0.5, y, z + 0.5, world.getRandom().nextFloat() * 360.0f, 0.0f);
					world.spawnEntity(collector);
					player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
							net.minecraft.entity.effect.StatusEffects.BLINDNESS, 100, 0, false, false, false));
				}
			}
		});
	}

	/**
	 * 开局礼包：首次进入世界赠送一枚不死图腾（覆盖首签成本）与一张死神债券，
	 * 并只弹一次温和提示——之后签不签全凭玩家自愿，不再打扰。
	 * 债券若丢失，上线时自动补发，等效于「永久持有」而无需防丢机制。
	 */
	private static void registerStarterKit() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			LifeDebtData data = LifeDebtAttachments.get(player);
			if (!data.isStarterContractGranted()) {
				player.giveItemStack(new ItemStack(Items.TOTEM_OF_UNDYING));
				data.setStarterContractGranted(true);
				player.sendMessage(Text.translatable("lifedebt.message.starter_bond"), false);
			}
			if (!hasReaperBond(player)) {
				player.giveItemStack(new ItemStack(ModItems.REAPER_BOND));
			}
			LifeDebtManager.updateContractPenalty(player);
		});
	}

	private static boolean hasReaperBond(ServerPlayerEntity player) {
		for (int slot = 0; slot < player.getInventory().size(); slot++) {
			if (player.getInventory().getStack(slot).isOf(ModItems.REAPER_BOND)) {
				return true;
			}
		}
		return false;
	}
}
