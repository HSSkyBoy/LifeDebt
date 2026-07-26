package top.nkbe.lifedebt.item;

import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.LifeDebt;
import top.nkbe.lifedebt.entity.ModEntities;

/** Items owned by the Life Debt gameplay loop. */
public final class ModItems {

	public static final Item DEBT_VOUCHER = register("debt_voucher", Item::new, new Item.Settings());
	public static final Item REAPER_BOND = register("reaper_bond", ReaperBondItem::new,
			new Item.Settings().maxCount(1));
	public static final Item DEBT_COLLECTOR_SPAWN_EGG = register("debt_collector_spawn_egg", SpawnEggItem::new,
			new Item.Settings().spawnEgg(ModEntities.DEBT_COLLECTOR));

	private ModItems() {
	}

	public static void initialize() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
			entries.add(DEBT_VOUCHER);
			entries.add(REAPER_BOND);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> entries.add(DEBT_COLLECTOR_SPAWN_EGG));
	}

	private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(LifeDebt.MOD_ID, name));
		return Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
	}
}
