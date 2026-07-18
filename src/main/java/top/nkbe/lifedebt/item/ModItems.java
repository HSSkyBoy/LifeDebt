package top.nkbe.lifedebt.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.LifeDebt;
import top.nkbe.lifedebt.entity.ModEntities;

/** Items owned by the Life Debt gameplay loop. */
public final class ModItems {

	public static final Item DEBT_VOUCHER = register("debt_voucher", new Item(new Item.Settings()));
	public static final Item DEBT_COLLECTOR_SPAWN_EGG = register("debt_collector_spawn_egg",
			new SpawnEggItem(ModEntities.DEBT_COLLECTOR, 0x111923, 0x9E1118, new Item.Settings()));

	private ModItems() {
	}

	public static void initialize() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(DEBT_VOUCHER));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> entries.add(DEBT_COLLECTOR_SPAWN_EGG));
	}

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, Identifier.of(LifeDebt.MOD_ID, name), item);
	}
}
