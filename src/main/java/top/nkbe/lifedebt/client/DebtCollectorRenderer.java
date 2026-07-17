package top.nkbe.lifedebt.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;
import top.nkbe.lifedebt.entity.DebtCollectorEntity;

@Environment(EnvType.CLIENT)
public class DebtCollectorRenderer extends BipedEntityRenderer<DebtCollectorEntity, BipedEntityModel<DebtCollectorEntity>> {

	public DebtCollectorRenderer(EntityRendererFactory.Context context) {
		super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
	}

	@Override
	public Identifier getTexture(DebtCollectorEntity entity) {
		return DebtCollectorEntity.texture();
	}
}
