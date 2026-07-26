package top.nkbe.lifedebt.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.LifeDebt;

public final class ModEntities {

	private static final RegistryKey<EntityType<?>> DEBT_COLLECTOR_KEY =
			RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(LifeDebt.MOD_ID, "debt_collector"));

	public static final EntityType<DebtCollectorEntity> DEBT_COLLECTOR = Registry.register(
			Registries.ENTITY_TYPE,
			DEBT_COLLECTOR_KEY,
			EntityType.Builder.create(DebtCollectorEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.6f, 1.95f)
					.build(DEBT_COLLECTOR_KEY));

	private ModEntities() {
	}

	public static void initialize() {
		DebtCollectorEntity.registerAttributes();
	}
}
