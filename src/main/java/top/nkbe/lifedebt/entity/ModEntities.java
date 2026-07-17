package top.nkbe.lifedebt.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.LifeDebt;

public final class ModEntities {

	public static final EntityType<DebtCollectorEntity> DEBT_COLLECTOR = Registry.register(
			Registries.ENTITY_TYPE,
			Identifier.of(LifeDebt.MOD_ID, "debt_collector"),
			EntityType.Builder.create(DebtCollectorEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.6f, 1.95f)
					.build("lifedebt:debt_collector"));

	private ModEntities() {
	}

	public static void initialize() {
		DebtCollectorEntity.registerAttributes();
	}
}
