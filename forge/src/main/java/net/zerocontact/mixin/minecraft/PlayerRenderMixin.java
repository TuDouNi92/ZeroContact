package net.zerocontact.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.zerocontact.armor.modular.module.nvg.event.ThermalRenderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRenderMixin {
    @Inject(
            method = "renderHand",
            at = @At(
                    value = "RETURN"
            )
    )
    private void renderHand(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            AbstractClientPlayer player,
            ModelPart rendererArm,
            ModelPart rendererArmwear,
            CallbackInfo ci) {
        ThermalRenderHandler.renderHandHeat(poseStack, combinedLight, player,
                rendererArm, rendererArmwear);
    }
}
