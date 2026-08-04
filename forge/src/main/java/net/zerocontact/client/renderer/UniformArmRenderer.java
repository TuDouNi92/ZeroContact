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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.events.EventUtil;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.*;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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
        ItemStack uniformStack = EventUtil.getCuriosStackFirst(player, "uniform_top");
        if (!(uniformStack.getItem() instanceof AbstractGenerateGeoCurioItemImpl abstractGenerateGeoCurioItem)) return;
        AccessoriesRender<?> baseRenderer = (AccessoriesRender<?>) CuriosRendererRegistry.getRenderer(abstractGenerateGeoCurioItem).orElse(null);
        if (baseRenderer == null) return;
        ArmorRender<?> renderer = baseRenderer.render;
        PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer((AbstractClientPlayer) player);
        BakedGeoModel bakedGeoModel = renderer.getGeoModel().getBakedGeoModel(abstractGenerateGeoCurioItem.model.toString());
        GeoBone armBone = arm == HumanoidArm.LEFT ? bakedGeoModel.getBone("armorLeftArm").orElse(null) : bakedGeoModel.getBone("armorRightArm").orElse(null);
        if (armBone == null) return;
        RenderType renderType = RenderType.armorCutoutNoCull(abstractGenerateGeoCurioItem.texture);
        poseStack.pushPose();
        poseStack.translate(0, 1.5f, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(0));
        poseStack.mulPose(Axis.XP.rotationDegrees(0));
        renderer.prepForRender(player, uniformStack, EquipmentSlot.CHEST, playerRenderer.getModel());
        renderBoneAndChildren(poseStack, armBone, bufferSource.getBuffer(renderType), packedLight, 1, 1, 1);
        poseStack.popPose();
        event.setCanceled(true);
    }

    private static void renderBoneAndChildren(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int light, float r, float g, float b) {
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

                    buffer.vertex(position.x(), position.y(), position.z(), r, g, b, 1.0F, vertex.texU(), vertex.texV(), OverlayTexture.NO_OVERLAY, light, normalVector.x(), normalVector.y(), normalVector.z());
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
