package top.nkbe.lifedebt.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.entity.damage.DamageSource;
import top.nkbe.lifedebt.core.DebtLevel;
import top.nkbe.lifedebt.core.LifeDebtAttachments;
import top.nkbe.lifedebt.item.ModItems;

public class DebtCollectorEntity extends HostileEntity {

	private static final Identifier TEXTURE = Identifier.of("lifedebt", "textures/entity/debt_collector.png");

	public DebtCollectorEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
	}

	public static void registerAttributes() {
		net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(ModEntities.DEBT_COLLECTOR, createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
				.build());
	}

	public static Identifier texture() {
		return TEXTURE;
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.8));
		this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(9, new LookAroundGoal(this));
		this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true,
				player -> player instanceof PlayerEntity target
						&& LifeDebtAttachments.get(target).getDebt() >= DebtLevel.DEBTOR.threshold));
	}

	@Override
	protected void dropLoot(DamageSource source, boolean causedByPlayer) {
		super.dropLoot(source, causedByPlayer);
		if (causedByPlayer) {
			this.dropStack(new ItemStack(ModItems.DEBT_VOUCHER));
		}
	}
}
