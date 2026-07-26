package top.nkbe.lifedebt.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import top.nkbe.lifedebt.net.OpenContractScreenPayload;

/**
 * 死神债券：开局即持有的签约入口。右键随时打开契约面板，
 * 签不签全凭自愿——它只是把「向死神借命」这扇门递到玩家手里。
 */
public class ReaperBondItem extends Item {

	public ReaperBondItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
			ServerPlayNetworking.send(serverPlayer, new OpenContractScreenPayload());
		}
		return TypedActionResult.success(stack, world.isClient());
	}
}
