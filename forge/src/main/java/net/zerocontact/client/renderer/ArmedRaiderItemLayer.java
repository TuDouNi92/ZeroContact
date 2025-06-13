package net.zerocontact.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.entity.ArmedRaider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Objects;

public class ArmedRaiderItemLayer extends BlockAndItemGeoLayer<ArmedRaider> {

    public ArmedRaiderItemLayer(GeoRenderer<ArmedRaider> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    protected @Nullable ItemStack getStackForBone(GeoBone bone, ArmedRaider animatable) {
        return animatable.getMainHandItem();
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, ArmedRaider animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        if (stack.isEmpty()|| Objects.requireNonNull(stack.getTag()).isEmpty()) return;
        renderRifle(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
    }

    private void renderRifle(PoseStack poseStack, GeoBone bone, ItemStack stack, ArmedRaider animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        if (bone.getName().equals("gun_fix")) {
            poseStack.pushPose();
            RenderUtils.prepMatrixForBone(poseStack, bone);
            poseStack.translate(0, -0.3125f, 0);
            poseStack.scale(0.625f, 0.625f, 0.625f);
            poseStack.mulPose(Axis.XN.rotationDegrees(90));
            super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }
}
