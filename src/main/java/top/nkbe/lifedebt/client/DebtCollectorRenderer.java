package top.nkbe.lifedebt.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.entity.DebtCollectorEntity;

@Environment(EnvType.CLIENT)
public class DebtCollectorRenderer
		extends MobEntityRenderer<DebtCollectorEntity, LivingEntityRenderState, DebtCollectorModel> {

	public DebtCollectorRenderer(EntityRendererFactory.Context context) {
		super(context, new DebtCollectorModel(context.getPart(DebtCollectorModel.LAYER)), 0.55f);
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	public Identifier getTexture(LivingEntityRenderState state) {
		return DebtCollectorEntity.texture();
	}
}
