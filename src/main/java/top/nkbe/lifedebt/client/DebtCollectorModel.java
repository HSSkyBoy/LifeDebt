package top.nkbe.lifedebt.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import top.nkbe.lifedebt.entity.DebtCollectorEntity;

/** A floating wraith, deliberately unrelated to the vanilla player silhouette. */
@Environment(EnvType.CLIENT)
public final class DebtCollectorModel extends SinglePartEntityModel<DebtCollectorEntity> {

	public static final EntityModelLayer LAYER = new EntityModelLayer(
			Identifier.of("lifedebt", "debt_collector"), "main");
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart tailLeft;
	private final ModelPart tailCenter;
	private final ModelPart tailRight;

	public DebtCollectorModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.tailLeft = root.getChild("tail_left");
		this.tailCenter = root.getChild("tail_center");
		this.tailRight = root.getChild("tail_right");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();

		root.addChild("head", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-4.0f, -10.0f, -4.0f, 8.0f, 8.0f, 8.0f)
				.uv(32, 0).cuboid(-5.0f, -11.0f, -5.0f, 10.0f, 3.0f, 10.0f)
				.uv(32, 12).cuboid(-5.0f, -8.0f, 3.0f, 10.0f, 5.0f, 2.0f),
			ModelTransform.pivot(0.0f, 3.0f, 0.0f));

		root.addChild("body", ModelPartBuilder.create()
				.uv(0, 32).cuboid(-4.0f, -7.0f, -2.0f, 8.0f, 9.0f, 4.0f)
				.uv(32, 32).cuboid(-5.0f, -7.0f, 1.5f, 10.0f, 13.0f, 1.0f),
			ModelTransform.pivot(0.0f, 11.0f, 0.0f));

		root.addChild("right_arm", ModelPartBuilder.create()
				.uv(64, 32).cuboid(-2.0f, -1.0f, -1.5f, 3.0f, 14.0f, 3.0f)
				.uv(80, 32).cuboid(-3.0f, 11.0f, -2.0f, 4.0f, 4.0f, 4.0f),
			ModelTransform.pivot(-6.0f, 6.0f, 0.0f));
		root.addChild("left_arm", ModelPartBuilder.create()
				.uv(64, 32).mirrored().cuboid(-1.0f, -1.0f, -1.5f, 3.0f, 14.0f, 3.0f)
				.uv(80, 32).mirrored().cuboid(-1.0f, 11.0f, -2.0f, 4.0f, 4.0f, 4.0f),
			ModelTransform.pivot(6.0f, 6.0f, 0.0f));

		root.addChild("tail_left", ModelPartBuilder.create()
				.uv(0, 64).cuboid(-4.0f, 0.0f, 0.5f, 4.0f, 12.0f, 1.0f),
			ModelTransform.pivot(-1.0f, 13.0f, 0.0f));
		root.addChild("tail_center", ModelPartBuilder.create()
				.uv(12, 64).cuboid(-2.0f, 0.0f, 0.5f, 4.0f, 15.0f, 1.0f),
			ModelTransform.pivot(0.0f, 13.0f, 0.0f));
		root.addChild("tail_right", ModelPartBuilder.create()
				.uv(24, 64).cuboid(0.0f, 0.0f, 0.5f, 4.0f, 12.0f, 1.0f),
			ModelTransform.pivot(1.0f, 13.0f, 0.0f));

		return TexturedModelData.of(data, 128, 128);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	@Override
	public void setAngles(DebtCollectorEntity entity, float limbAngle, float limbDistance,
			float animationProgress, float headYaw, float headPitch) {
		head.yaw = headYaw * 0.011f;
		head.pitch = 0.2f + headPitch * 0.011f;
		rightArm.pitch = 0.25f + MathHelper.cos(animationProgress * 0.12f) * 0.16f;
		leftArm.pitch = 0.25f + MathHelper.cos(animationProgress * 0.12f + MathHelper.PI) * 0.16f;
		body.yaw = MathHelper.sin(animationProgress * 0.05f) * 0.035f;
		tailLeft.yaw = -0.14f + MathHelper.sin(animationProgress * 0.09f) * 0.08f;
		tailCenter.yaw = MathHelper.sin(animationProgress * 0.08f) * 0.06f;
		tailRight.yaw = 0.14f + MathHelper.sin(animationProgress * 0.09f + MathHelper.PI) * 0.08f;
	}
}
