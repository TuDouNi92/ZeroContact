package net.zerocontact.renderer;

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
        if (stack.isEmpty()) return;
        if (bone.getName().equals("RightArm")) {
            poseStack.pushPose();
            RenderUtils.prepMatrixForBone(poseStack, bone);
            poseStack.scale(0.625f,0.625f,0.625f);
            poseStack.translate(0,-1.5f,0);
            poseStack.mulPose(Axis.XN.rotationDegrees(90));
            super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }
}
