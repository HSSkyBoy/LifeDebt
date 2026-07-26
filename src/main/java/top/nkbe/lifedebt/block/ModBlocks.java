package top.nkbe.lifedebt.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.LifeDebt;

/**
 * 命债方块注册。当前仅有「债务祭坛」——还债机制的唯一交互入口。
 */
public final class ModBlocks {

	/** 债务祭坛：玩家手持图腾或债券对其右键以偿还借命。交互逻辑见 event 层。 */
	public static final Block DEBT_ALTAR = register("debt_altar");

	private ModBlocks() {
	}

	/** 触发类加载以完成注册。 */
	public static void initialize() {
	}

	private static Block register(String name) {
		Identifier id = Identifier.of(LifeDebt.MOD_ID, name);
		RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
		Block block = new Block(AbstractBlock.Settings.create()
				.strength(3.5f)
				.sounds(BlockSoundGroup.STONE)
				.registryKey(blockKey));
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
		Registry.register(Registries.ITEM, itemKey,
				new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey()));
		return Registry.register(Registries.BLOCK, blockKey, block);
	}
}
