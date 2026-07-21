package net.zerocontact.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.client.interaction.SuppressionManager;

public class PlayerSuppressionOverlay {
    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation(ZeroContact.MOD_ID,"textures/gui/suppression.png");
    public static final IGuiOverlay SUPPRESSION_OVERLAY = (forgeGui, graphics, ticks, w, h) -> {
        float suppressionLevel = SuppressionManager.suppressionLevel;
        if (suppressionLevel > 0) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, VIGNETTE_LOCATION);
            RenderSystem.setShaderColor(1, 1, 1, suppressionLevel);
            graphics.blit(VIGNETTE_LOCATION, 0, 0, w, h, 0, 0, w, h, w, h);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    };

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class Listener {
        @SubscribeEvent
        public static void onRegisterOverlay(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("zc_suppression_overlay",SUPPRESSION_OVERLAY);
        }
    }
}
