package net.zerocontact.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.animation_data.AnimateData;
import net.zerocontact.api.Toggleable;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class VisorHUD {
    private static String lastPlayedAnim = "";
    static float hudOffsetProgress = 0;
    static int noNameAnimTick = 0;
    static double animStartTick = 0;
    static double noNameCache = 0;
    static double relativeTick = 0;
    static boolean locked = false;
    static float yCor = 0;

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof Toggleable toggleable) {
            drawHUD(event, mc);
        }
    }

    private static void drawHUD(RenderGuiEvent event, Minecraft mc) {
        GuiGraphics graphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        if (mc.player == null) return;
        if (mc.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof Toggleable toggleable) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            blitToggleableVisor(toggleable, height, graphics, width);
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    private static void blitToggleableVisor(Toggleable toggleable, int height, GuiGraphics graphics, int width) {
        if (toggleable.getAnimData() != null) {
            AnimateData.VisorAnimateData animateData = toggleable.getAnimData();
            String name = animateData.animationName;
            if (name.isEmpty()) {
                noNameAnimTick++;
            }
            double tick = animateData.tick;
            float actuallyStoppedFramePercent = 0.5f;
            double visorAnimLength = Math.max(animateData.animLength * actuallyStoppedFramePercent, 1.0);
            if (!lastPlayedAnim.equals(animateData.animationName) || noNameCache != noNameAnimTick) {
                noNameCache = noNameAnimTick;
                lastPlayedAnim = animateData.animationName;
                locked = false;
            }
            relativeTick = tick - animStartTick;
            double progress = relativeTick / visorAnimLength;
            hudOffsetProgress = (float) Mth.clamp(progress, 0.001f, 1f);
            if (hudOffsetProgress >= 1.0f) {
                locked = true;
                relativeTick = 0;
            }
            float offset = height * hudOffsetProgress;
            if (name.equals("switch_disabled_to_enabled")) {
                yCor = -height + offset;
            } else {
                yCor = -offset;
            }
            //TODO No first person functionality
            //TODO Flickers on enable
            //TODO Holding helmet in hand also triggers HUD wtf
            graphics.blit(toggleable.getVisorTexture(), 0, (int) Math.ceil(yCor), -100, 0, 0, width, height, width, height);
        }
    }
}
