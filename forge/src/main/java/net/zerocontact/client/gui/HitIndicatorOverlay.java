package net.zerocontact.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.events.ZDamageTypes;
import net.zerocontact.forge.EnvHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HitIndicatorOverlay {

    static class HurtRecord {
        private final Vec3 targetPos;
        private final float amount;
        private int timer;

        HurtRecord(LivingEntity target, float amount, int timer) {
            this.targetPos = target.position();
            this.amount = amount;
            this.timer = timer;
        }
    }

    private static final ArrayList<HurtRecord> targetEntities = new ArrayList<>();
    private static final int RESIDENCE_TICKS = 90;

    @SubscribeEvent
    public static void livingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source.is(ZDamageTypes.ZC_DAMAGE)) {
            targetEntities.add(new HurtRecord(event.getEntity(), event.getAmount(), 0));
        }
    }

    @SubscribeEvent
    public static void renderInLevel(RenderLevelStageEvent event) {
        if(!EnvHelper.DEBUG)return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        PoseStack poseStack = event.getPoseStack();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        Iterator<HurtRecord> iterator = targetEntities.iterator();
        while (iterator.hasNext()) {
            HurtRecord record = iterator.next();
            record.timer++;
            if (record.timer >= RESIDENCE_TICKS) {
                iterator.remove();
            }
        }
        for (HurtRecord record1 : List.copyOf(targetEntities)) {
            float progress = (float) record1.timer/RESIDENCE_TICKS;
            double yOffset = progress / 2;
            poseStack.pushPose();
            poseStack.translate(
                    record1.targetPos.x - cameraPos.x,
                    record1.targetPos.y+2 + yOffset - cameraPos.y,
                    record1.targetPos.z - cameraPos.z
            );
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(
                    -0.025F,
                    -0.025F,
                    0.025F
            );
            MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
            String text = String.format("%.2f",record1.amount);
            float textX = (float) -mc.font.width(text) / 2;
            RenderSystem.disableDepthTest();
            mc.font.drawInBatch(
                    text,
                    textX,
                    0,
                    0xFFE135,
                    false,
                    poseStack.last().pose(),
                    buffer,
                    Font.DisplayMode.NORMAL,
                    0,
                    15728880
            );
            buffer.endBatch();
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }
}
