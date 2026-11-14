package net.zerocontact.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.caliber.damage.ZDamageTypes;
import net.zerocontact.forge.EnvHelper;

import java.util.ArrayList;
import java.util.Locale;

@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
public class HitIndicatorOverlay {

    static class HurtRecord {
        private final Vec3 targetPos;
        private final int targetId;
        private final float amount;
        private final int displaySlot;
        private int timer;

        HurtRecord(Vec3 targetPos, int targetId, float amount, int displaySlot) {
            this.targetPos = targetPos;
            this.targetId = targetId;
            this.amount = amount;
            this.displaySlot = displaySlot;
        }
    }

    private static final ArrayList<HurtRecord> targetEntities = new ArrayList<>();
    private static final int RESIDENCE_TICKS = 90;
    private static final int DISPLAY_COLUMNS = 3;
    private static final int[] DISPLAY_COLUMN_OFFSETS = {0, -1, 1};
    private static final float DISPLAY_COLUMN_SPACING = 36.0F;
    private static final float DISPLAY_ROW_SPACING = 11.0F;

    @SubscribeEvent
    public static void livingHurt(LivingIncomingDamageEvent event) {
        if (!EnvHelper.DEBUG) return;
        DamageSource source = event.getSource();
        if (source.is(ZDamageTypes.ZC_DAMAGE) || source.is(ModDamageTypes.BULLETS_TAG)) {
            LivingEntity target = event.getEntity();
            Vec3 targetPos = target.position();
            int targetId = target.getId();
            float amount = event.getAmount();
            // Damage events also run on the integrated server thread; keep the render list client-owned.
            Minecraft.getInstance().execute(() -> targetEntities.add(
                    new HurtRecord(targetPos, targetId, amount, findAvailableDisplaySlot(targetId))));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            targetEntities.clear();
            return;
        }
        if (!mc.isPaused()) {
            targetEntities.removeIf(record -> ++record.timer >= RESIDENCE_TICKS);
        }
    }

    private static int findAvailableDisplaySlot(int targetId) {
        int displaySlot = 0;
        while (true) {
            int candidate = displaySlot;
            boolean occupied = targetEntities.stream()
                    .anyMatch(record -> record.targetId == targetId && record.displaySlot == candidate);
            if (!occupied) {
                return displaySlot;
            }
            displaySlot++;
        }
    }

    @SubscribeEvent
    public static void renderInLevel(RenderLevelStageEvent event) {
        if (!EnvHelper.DEBUG) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || targetEntities.isEmpty()) return;
        // AFTER_LEVEL runs after LevelRenderer restores the global model-view stack.
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(event.getModelViewMatrix());
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        for (HurtRecord record1 : targetEntities) {
            float progress = (record1.timer + partialTick) / RESIDENCE_TICKS;
            double yOffset = progress / 2;
            poseStack.pushPose();
            poseStack.translate(
                    record1.targetPos.x - cameraPos.x,
                    record1.targetPos.y+2 + yOffset - cameraPos.y,
                    record1.targetPos.z - cameraPos.z
            );
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            // Match 1.21.1 name tags: a negative X scale reverses glyph winding and gets culled.
            poseStack.scale(
                    0.025F,
                    -0.025F,
                    0.025F
            );
            String text = String.format(Locale.ROOT, "%.2f", record1.amount);
            int column = record1.displaySlot % DISPLAY_COLUMNS;
            int row = record1.displaySlot / DISPLAY_COLUMNS;
            float textX = (float) -mc.font.width(text) / 2
                    + DISPLAY_COLUMN_OFFSETS[column] * DISPLAY_COLUMN_SPACING;
            float textY = -row * DISPLAY_ROW_SPACING;
            mc.font.drawInBatch(
                    text,
                    textX,
                    textY,
                    0xFFFFE135,
                    false,
                    poseStack.last().pose(),
                    buffer,
                    // NORMAL re-enables depth testing when its RenderType is flushed.
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    LightTexture.FULL_BRIGHT
            );
            poseStack.popPose();
        }
        buffer.endBatch();
    }
}
