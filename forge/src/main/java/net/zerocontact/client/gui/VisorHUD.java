package net.zerocontact.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.animation_data.AnimateData;
import net.zerocontact.api.Toggleable;

import java.util.Optional;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class VisorHUD {
    private static int yCor;
    private static int currentTick;
    private static String cachedAnim;

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        drawHUD(event, mc);
        trickyKeepAnimationLoaded(mc, event.getGuiGraphics(), 0, 0, 0, event.getPartialTick());
    }

    private static void drawHUD(RenderGuiEvent event, Minecraft mc) {
        GuiGraphics graphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        if (mc.player == null) return;
        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof Toggleable toggleable) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.defaultBlendFunc();
            blitToggleableVisor(toggleable, helmet, height, graphics, width);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    private static void blitToggleableVisor(Toggleable toggleable, ItemStack visorStack, int height, GuiGraphics graphics, int width) {
        if (toggleable.readAnimData(visorStack) != null) {
            AnimateData.VisorAnimateData animateData = toggleable.readAnimData(visorStack);
            String animName = animateData.animationName;
            double animLength = Math.ceil(animateData.animLength);
            if (!animName.equals(cachedAnim)) {
                currentTick = 0;
            }
            double progress = currentTick / animLength;
            currentTick++;
            if (currentTick > animLength) {
                currentTick = Mth.floor(animLength);
            }
            if(animName.isEmpty() || animLength<=0)return;
            if (animName.equals("switch_disabled_to_enabled")) {
                yCor = Mth.floor(progress * height - height);
                RenderSystem.setShaderColor(1, 1, 1, (float) progress);
            } else {
                yCor = Mth.floor(-progress * height);
                RenderSystem.setShaderColor(1, 1, 1, (float) (1 - progress));
            }
            cachedAnim = animName;
            RenderSystem.setShaderTexture(0, toggleable.getVisorTexture());
            graphics.blit(toggleable.getVisorTexture(), 0, yCor, -100, 0, 0, width, height, width, height);
        }
    }

    private static void trickyKeepAnimationLoaded(Minecraft mc, GuiGraphics guiGraphics, double x, double y, double z, float partialTicks) {
        Optional.ofNullable(mc.player).ifPresent(
                player -> {
                    EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
                    dispatcher.render(player, x, y, z, 0, partialTicks, guiGraphics.pose(), guiGraphics.bufferSource(), 10485760);
                });
    }
}
