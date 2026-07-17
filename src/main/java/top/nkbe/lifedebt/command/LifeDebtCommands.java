package top.nkbe.lifedebt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import top.nkbe.lifedebt.block.ModBlocks;
import top.nkbe.lifedebt.core.LifeDebtAttachments;
import top.nkbe.lifedebt.core.LifeDebtData;
import top.nkbe.lifedebt.entity.DebtCollectorEntity;
import top.nkbe.lifedebt.entity.ModEntities;
import top.nkbe.lifedebt.net.LifeDebtNetworking;

/**
 * 调试 / 测试指令（需要 OP，权限等级 2）。当前提供：
 * <ul>
 *   <li>{@code /lifedebt altar}：在脚下放置一座债务祭坛。</li>
 *   <li>{@code /lifedebt collector}：在脚下生成一只追债者。</li>
 *   <li>{@code /lifedebt debt <值>}：直接设置自己的债务值，便于测试等级效果。</li>
 * </ul>
 * 追债者仅索敌债务 ≥ {@code DEBTOR}（5）的玩家，若要测试其追击需先把债务顶到阈值。
 */
public final class LifeDebtCommands {

	private LifeDebtCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("lifedebt")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.literal("altar").executes(LifeDebtCommands::placeAltar))
						.then(CommandManager.literal("collector").executes(LifeDebtCommands::spawnCollector))
						.then(CommandManager.literal("debt")
								.then(CommandManager.argument("value", IntegerArgumentType.integer(0))
										.executes(LifeDebtCommands::setDebt)))));
	}

	private static int setDebt(com.mojang.brigadier.context.CommandContext<ServerCommandSource> ctx)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		int value = IntegerArgumentType.getInteger(ctx, "value");
		LifeDebtData data = LifeDebtAttachments.get(player);
		data.setDebt(value);
		LifeDebtNetworking.syncState(player);
		ctx.getSource().sendFeedback(
				() -> Text.literal("债务已设为 " + data.getDebt() + "（等级：" + data.getLevel() + "）。"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int placeAltar(com.mojang.brigadier.context.CommandContext<ServerCommandSource> ctx)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		ServerWorld world = player.getServerWorld();
		BlockPos pos = player.getBlockPos();
		world.setBlockState(pos, ModBlocks.DEBT_ALTAR.getDefaultState());
		ctx.getSource().sendFeedback(
				() -> Text.literal("已在 " + pos.toShortString() + " 放置债务祭坛。"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int spawnCollector(com.mojang.brigadier.context.CommandContext<ServerCommandSource> ctx)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		ServerWorld world = player.getServerWorld();
		DebtCollectorEntity collector = ModEntities.DEBT_COLLECTOR.create(world);
		if (collector == null) {
			ctx.getSource().sendError(Text.literal("追债者生成失败。"));
			return 0;
		}
		collector.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(),
				player.getYaw(), 0.0f);
		world.spawnEntity(collector);
		ctx.getSource().sendFeedback(() -> Text.literal("已生成追债者。"), false);
		return Command.SINGLE_SUCCESS;
	}
}
