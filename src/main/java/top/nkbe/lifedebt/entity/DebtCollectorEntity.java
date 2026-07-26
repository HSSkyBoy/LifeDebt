package top.nkbe.lifedebt.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import top.nkbe.lifedebt.core.DebtLevel;
import top.nkbe.lifedebt.core.LifeDebtAttachments;
import top.nkbe.lifedebt.item.ModItems;

public class DebtCollectorEntity extends HostileEntity {

	private static final Identifier TEXTURE = Identifier.of("lifedebt", "textures/entity/debt_collector.png");
	private int daylightDepartureTicks = -1;

	public DebtCollectorEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
		this.moveControl = new FlightMoveControl(this, 35, true);
		this.setNoGravity(true);
	}

	public static void registerAttributes() {
		FabricDefaultAttributeRegistry.register(ModEntities.DEBT_COLLECTOR, createHostileAttributes()
				.add(EntityAttributes.MAX_HEALTH, 30.0)
				.add(EntityAttributes.MOVEMENT_SPEED, 0.42)
				.add(EntityAttributes.FLYING_SPEED, 0.85)
				.add(EntityAttributes.ATTACK_DAMAGE, 5.0)
				.add(EntityAttributes.FOLLOW_RANGE, 32.0));
	}

	public static Identifier texture() {
		return TEXTURE;
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(2, new MeleeAttackGoal(this, 1.65, false));
		this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.8));
		this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(9, new LookAroundGoal(this));
		this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true,
				(target, world) -> target instanceof PlayerEntity player
						&& LifeDebtAttachments.get(player).getDebt() >= DebtLevel.DEBTOR.threshold));
	}

	@Override
	protected EntityNavigation createNavigation(World world) {
		BirdNavigation navigation = new BirdNavigation(this, world);
		navigation.setCanOpenDoors(true);
		navigation.setCanSwim(true);
		return navigation;
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
		if (this.getEntityWorld() instanceof ServerWorld world && !world.isNight()) {
			this.daylightDepartureTicks++;
			this.getNavigation().stop();
			this.setVelocity(0.0, 0.0, 0.0);
			this.setYaw(this.getYaw() + 28.0f);
			world.spawnParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.8, this.getZ(),
					5, 0.35, 0.5, 0.35, 0.02);
			if (this.daylightDepartureTicks >= 20) {
				this.discard();
			}
			return;
		}
		this.daylightDepartureTicks = -1;
		if (this.getTarget() instanceof ServerPlayerEntity player) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0, false, false, false));
		}
	}

	@Override
	protected void dropLoot(ServerWorld world, DamageSource source, boolean causedByPlayer) {
		super.dropLoot(world, source, causedByPlayer);
		if (causedByPlayer) {
			this.dropStack(world, new ItemStack(ModItems.DEBT_VOUCHER, 2));
		}
	}
}
