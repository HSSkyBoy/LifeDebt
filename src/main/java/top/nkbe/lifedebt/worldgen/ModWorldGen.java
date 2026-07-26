package top.nkbe.lifedebt.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import top.nkbe.lifedebt.LifeDebt;

/** Registers the rare overworld debt-altar ruin. */
public final class ModWorldGen {
	private static final Identifier DEBT_ALTAR_RUIN_ID = Identifier.of(LifeDebt.MOD_ID, "debt_altar_ruin");
	public static final Feature<DefaultFeatureConfig> DEBT_ALTAR_RUIN = Registry.register(
			Registries.FEATURE, DEBT_ALTAR_RUIN_ID, new DebtAltarRuinFeature(DefaultFeatureConfig.CODEC));
	public static final RegistryKey<PlacedFeature> DEBT_ALTAR_RUIN_PLACED_KEY =
			RegistryKey.of(RegistryKeys.PLACED_FEATURE, DEBT_ALTAR_RUIN_ID);

	private ModWorldGen() {
	}

	public static void initialize() {
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.SURFACE_STRUCTURES, DEBT_ALTAR_RUIN_PLACED_KEY);
	}
}
