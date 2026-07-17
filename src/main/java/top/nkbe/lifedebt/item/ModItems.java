package top.nkbe.lifedebt.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.LifeDebt;

/** Items owned by the Life Debt gameplay loop. */
public final class ModItems {

	public static final Item DEBT_VOUCHER = register("debt_voucher", new Item(new Item.Settings()));

	private ModItems() {
	}

	public static void initialize() {
	}

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, Identifier.of(LifeDebt.MOD_ID, name), item);
	}
}
