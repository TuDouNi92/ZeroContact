package net.zerocontact.armor.modular.module.pouch.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.zerocontact.armor.modular.module.pouch.container.navboard.NavBoardContainer;
import net.zerocontact.capability.CapabilityRegistries;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

/** The first cube's south face in a bone named "screen" defines the display surface. */
public final class NavBoardScreenLayer<T extends Item & GeoItem> extends GeoRenderLayer<T> {
    public static final String SCREEN_BONE = "screen";
    private static final float SURFACE_OFFSET = 0.0005f;

    public NavBoardScreenLayer(GeoRenderer<T> renderer) { super(renderer); }

    @Override
    public void renderForBone(PoseStack poses, T animatable, GeoBone bone, RenderType originalType,
                              MultiBufferSource buffers, VertexConsumer originalBuffer,
                              float partialTick, int packedLight, int packedOverlay) {
        if (!SCREEN_BONE.equals(bone.getName()) || bone.isHidden() || bone.getCubes().isEmpty()
                || !(getRenderer() instanceof GeoItemRenderer<?> itemRenderer)) return;
        var stack = itemRenderer.getCurrentItemStack();
        if (stack == null || stack.isEmpty()
                || !stack.getCapability(CapabilityRegistries.NAV_BOARD).map(NavBoardContainer::isEnabled).orElse(false)) return;
        var screen = NavBoardScreens.forStack(stack);
        if (screen == null) return; // Remote wearers and unsampled terminals retain their model texture.

        var cube = bone.getCubes().get(0);
        GeoQuad face = null;
        for (var quad : cube.quads()) {
            if (quad != null && quad.direction() == Direction.SOUTH) { face = quad; break; }
        }
        if (face == null) return;
        // Baked vertices already use model units (1 block = 16 Blockbench units).
        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (var vertex : face.vertices()) {
            minX = Math.min(minX, vertex.position().x()); maxX = Math.max(maxX, vertex.position().x());
            minY = Math.min(minY, vertex.position().y()); maxY = Math.max(maxY, vertex.position().y());
        }
        if (maxX <= minX || maxY <= minY) return;
        poses.pushPose();
        try {
            // renderForBone already includes all bone transforms; only cube transforms remain.
            RenderUtils.translateToPivotPoint(poses, cube);
            RenderUtils.rotateMatrixAroundCube(poses, cube);
            RenderUtils.translateAwayFromPivotPoint(poses, cube);
            var pose = poses.last();
            var normal = face.normal();
            var buffer = buffers.getBuffer(screen.renderType());
            for (var vertex : face.vertices()) {
                var position = vertex.position();
                // South is viewed from +Z: right is +X. FBO's bottom-left origin reverses V.
                float u = (position.x() - minX) / (maxX - minX);
                float v = (position.y() - minY) / (maxY - minY);
                buffer.vertex(pose.pose(), position.x() + normal.x() * SURFACE_OFFSET,
                                position.y() + normal.y() * SURFACE_OFFSET, position.z() + normal.z() * SURFACE_OFFSET)
                        .color(255, 255, 255, 255).uv(u, v)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                        .normal(pose.normal(), normal.x(), normal.y(), normal.z()).endVertex();
            }
        } finally {
            poses.popPose();
            buffers.getBuffer(originalType);
        }
    }
}
