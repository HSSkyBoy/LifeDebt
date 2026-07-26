package top.nkbe.lifedebt.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import top.nkbe.lifedebt.block.ModBlocks;

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
		return true;
	}
}
