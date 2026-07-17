package top.nkbe.lifedebt.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/** Resolves the mapped world accessor across supported Minecraft versions. */
public final class LifeDebtWorldHelper {

	private LifeDebtWorldHelper() {
	}

	public static World getWorld(PlayerEntity player) {
		//? if <1.18 {
		/*return player.getEntityWorld();
		*///?} elif <1.21.9 {
		return player.getWorld();
		//?} else {
		/*return player.getEntityWorld();
		*///?}
	}

	public static boolean isClient(PlayerEntity player) {
		//? if <1.21.9 {
		return getWorld(player).isClient;
		//?} else {
		/*return getWorld(player).isClient();
		*///?}
	}
}
