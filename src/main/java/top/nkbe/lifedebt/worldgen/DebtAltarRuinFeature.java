package top.nkbe.lifedebt.worldgen;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import top.nkbe.lifedebt.block.ModBlocks;
import top.nkbe.lifedebt.item.ModItems;

/** A small, rare surface ruin that gives the debt altar a discoverable home. */
public final class DebtAltarRuinFeature extends Feature<DefaultFeatureConfig> {
	public DebtAltarRuinFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		StructureWorldAccess world = context.getWorld();
		BlockPos origin = context.getOrigin();
		int x = origin.getX();
		int z = origin.getZ();
		int groundY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z) - 1;
		BlockPos ground = new BlockPos(x, groundY, z);

		if (groundY <= world.getBottomY() || !world.getFluidState(ground).isEmpty()
				|| !world.getBlockState(ground).isSolidBlock(world, ground)) {
			return false;
		}

		// Do not bury the shrine in a tree, cliff, or existing structure.
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				BlockPos check = ground.add(dx, 1, dz);
				if (!world.isAir(check) || !world.isAir(check.up())) {
					return false;
				}
			}
		}

		BlockState[] stones = {
				Blocks.STONE_BRICKS.getDefaultState(),
				Blocks.STONE_BRICKS.getDefaultState(),
				Blocks.MOSSY_STONE_BRICKS.getDefaultState(),
				Blocks.CRACKED_STONE_BRICKS.getDefaultState()
		};
		BlockPos floor = ground.up();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				world.setBlockState(floor.add(dx, 0, dz), stones[context.getRandom().nextInt(stones.length)], Block.NOTIFY_LISTENERS);
			}
		}

		// Broken corner pylons make the altar readable from a distance without becoming a large structure.
		for (BlockPos corner : new BlockPos[] {
				floor.add(-2, 1, -2), floor.add(2, 1, -2), floor.add(-2, 1, 2), floor.add(2, 1, 2)
		}) {
			if (context.getRandom().nextBoolean()) {
				world.setBlockState(corner, stones[context.getRandom().nextInt(stones.length)], Block.NOTIFY_LISTENERS);
			}
		}
		world.setBlockState(floor.up(), ModBlocks.DEBT_ALTAR.getDefaultState(), Block.NOTIFY_LISTENERS);

		// 叙事箱：书卷交代「向死神借命」的世界观，旧债券暗示偿还经济。探索者自行发现，不打扰纯玩法玩家。
		BlockPos chestPos = floor.add(1, 1, 1);
		world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), Block.NOTIFY_LISTENERS);
		if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
			chest.setStack(11, new ItemStack(ModItems.DEBT_VOUCHER, 1 + context.getRandom().nextInt(2)));
			chest.setStack(13, createLoreBook());
		}
		return true;
	}

	/** 前任借命者留下的账册；页面走翻译键，随客户端语言呈现。 */
	private static ItemStack createLoreBook() {
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
		book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
				RawFilteredPair.of("……"), "?", 0,
				List.of(RawFilteredPair.of(Text.translatable("lifedebt.book.ruin_page1")),
						RawFilteredPair.of(Text.translatable("lifedebt.book.ruin_page2"))),
				true));
		return book;
	}
}
