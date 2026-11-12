package net.zerocontact.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.zerocontact.ZeroContact;
import net.zerocontact.client.interaction.SuppressionManager;

public class PlayerSuppressionOverlay {
    private static final ResourceLocation VIGNETTE_LOCATION = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "textures/gui/suppression.png");
    public static final LayeredDraw.Layer SUPPRESSION_OVERLAY = (forgeGui, deltaTracker) -> {
        float suppressionLevel = SuppressionManager.suppressionLevel;
        if (suppressionLevel > 0) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, VIGNETTE_LOCATION);
            RenderSystem.setShaderColor(1, 1, 1, suppressionLevel);
            forgeGui.blit(VIGNETTE_LOCATION, 0, 0, forgeGui.guiWidth(), forgeGui.guiHeight(), 0, 0, forgeGui.guiWidth(), forgeGui.guiHeight(), forgeGui.guiWidth(), forgeGui.guiHeight());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    };

    @EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
    static class Listener {
        @SubscribeEvent
        public static void onRegisterOverlay(RegisterGuiLayersEvent event) {
            event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "zc_suppression_overlay"), SUPPRESSION_OVERLAY);
        }
    }
}
