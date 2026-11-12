package net.zerocontact.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.curios.CuriosConstants;
import net.zerocontact.events.EventUtil;
import net.zerocontact.armor.modular.module.nvg.event.ThermalRenderHandler;
import net.zerocontact.item.neoforge.AbstractGenerateGeoCurioItemImpl;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.*;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
public class UniformArmRenderer {
    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        Player player = event.getPlayer();
        HumanoidArm arm = event.getArm();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource source = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        renderArm(player, arm, poseStack, source, packedLight, event);
    }

    private static void renderArm(Player player, HumanoidArm arm, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, RenderArmEvent event) {
        ItemStack uniformStack = EventUtil.getCuriosStackFirst(player, CuriosConstants.UNIFORM_TOP);
        if (!(uniformStack.getItem() instanceof AbstractGenerateGeoCurioItemImpl abstractGenerateGeoCurioItem)) return;
        AccessoriesRender<?> baseRenderer = (AccessoriesRender<?>) CuriosRendererRegistry.getRenderer(abstractGenerateGeoCurioItem).orElse(null);
        if (baseRenderer == null) return;
        ArmorRender<?> renderer = baseRenderer.render;
        PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer((AbstractClientPlayer) player);
        BakedGeoModel bakedGeoModel = renderer.getGeoModel().getBakedModel(abstractGenerateGeoCurioItem.model);
        GeoBone armBone = arm == HumanoidArm.LEFT ? bakedGeoModel.getBone("armorLeftArm").orElse(null) : bakedGeoModel.getBone("armorRightArm").orElse(null);
        if (armBone == null) return;
        RenderType renderType = RenderType.armorCutoutNoCull(abstractGenerateGeoCurioItem.texture);
        poseStack.pushPose();
        poseStack.translate(0, 1.5f, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(0));
        poseStack.mulPose(Axis.XP.rotationDegrees(0));
        final Minecraft mc = Minecraft.getInstance();
        renderer.prepForRender(
                player,
                uniformStack,
                EquipmentSlot.CHEST,
                playerRenderer.getModel(),
                bufferSource,
                mc.getTimer().getGameTimeDeltaPartialTick(true),
                0,
                0,
                0,
                0
                );

        renderBoneAndChildren(poseStack, armBone, bufferSource.getBuffer(renderType), packedLight, 255, 255, 255);
        // This event cancels PlayerRenderer.renderHand, so its thermal mixin never runs.
        // Reuse the same prepared bone and live pose, including TaCZ's hand animation.
        ThermalRenderHandler.renderHandHeat(heatBuffers ->
                renderBoneAndChildren(poseStack, armBone, heatBuffers.getBuffer(renderType), packedLight, 255, 255, 255));
        poseStack.popPose();
        event.setCanceled(true);
    }

    // RGB channels use the 0-255 range expected by FastColor.ARGB32.
    private static void renderBoneAndChildren(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int light, int r, int g, int b) {
        if (bone.isHidden()) return;

        for (GeoCube cube : bone.getCubes()) {
            poseStack.pushPose();
            PoseStack.Pose lastPose = poseStack.last();
            Matrix4f pose = lastPose.pose();
            Matrix3f normal = lastPose.normal();

            for (GeoQuad quad : cube.quads()) {
                Vector3f normalVector = new Vector3f(quad.normal());
                normal.transform(normalVector);
                for (GeoVertex vertex : quad.vertices()) {
                    Vector4f position = new Vector4f(vertex.position().x(), vertex.position().y(), vertex.position().z(), 1.0F);
                    pose.transform(position);
                    buffer.addVertex(
                            position.x(), position.y(), position.z(),
                            FastColor.ARGB32.color(255, r, g, b),
                            vertex.texU(), vertex.texV(),
                            OverlayTexture.NO_OVERLAY,
                            light,
                            normalVector.x(), normalVector.y(), normalVector.z());
                }
            }
            poseStack.popPose();
        }

        for (GeoBone childBone : bone.getChildBones()) {
            if (!childBone.isHidden() && !bone.isHidingChildren()) {
                renderBoneAndChildren(poseStack, childBone, buffer, light, r, g, b);
            }
        }
    }
}
