package net.zerocontact.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.models.ArmedRaiderModel;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ArmedRaiderRender extends GeoEntityRenderer<ArmedRaider> {
    public ArmedRaiderRender(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ArmedRaiderModel());
        this.addRenderLayer(new ArmedRaiderItemLayer(this));
    }

    @Override
    public void render(@NotNull ArmedRaider entity, float entityYaw, float partialTick, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(1,0.9f,1);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
