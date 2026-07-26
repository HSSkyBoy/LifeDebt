package top.nkbe.lifedebt.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.nkbe.lifedebt.core.ContractEffects;

/**
 * 魂契常驻收益：经验获取加成。放大所有走 {@code addExperience} 的正向经验
 * （经验球、熔炉、交易等）；负数与直接的 addExperienceLevels（如魂契付费）不受影响。
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityXpMixin {

	@ModifyVariable(method = "addExperience", at = @At("HEAD"), argsOnly = true)
	private int lifedebt$soulPactXp(int experience) {
		if (experience > 0 && (Object) this instanceof ServerPlayerEntity player) {
			return (int) Math.round(experience * ContractEffects.xpMultiplier(player));
		}
		return experience;
	}
}
