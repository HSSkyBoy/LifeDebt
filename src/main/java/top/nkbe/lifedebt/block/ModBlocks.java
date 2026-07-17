package top.nkbe.lifedebt.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.LifeDebt;

/**
 * 命债方块注册。当前仅有「债务祭坛」——还债机制的唯一交互入口。
 */
public final class ModBlocks {

	/** 债务祭坛：玩家手持图腾或债券对其右键以偿还借命。交互逻辑见 event 层。 */
	public static final Block DEBT_ALTAR = register("debt_altar",
			new Block(AbstractBlock.Settings.create()
					.strength(3.5f)
					.sounds(BlockSoundGroup.STONE)));

	private ModBlocks() {
	}

	/** 触发类加载以完成注册。 */
	public static void initialize() {
	}

	private static Block register(String name, Block block) {
		Identifier id = Identifier.of(LifeDebt.MOD_ID, name);
		Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
		return Registry.register(Registries.BLOCK, id, block);
	}
}
