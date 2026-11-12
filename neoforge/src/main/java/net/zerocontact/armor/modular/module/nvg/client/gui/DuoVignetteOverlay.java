package net.zerocontact.armor.modular.module.nvg.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.armor.modular.module.nvg.client.ClientInteractionManger;

@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
public final class DuoVignetteOverlay {
    public static final LayeredDraw.Layer DUO_VIGNETTE = (forgeGui, deltaTracker) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.isSpectator()
                || minecraft.options.hideGui || !minecraft.options.getCameraType().isFirstPerson()
                || minecraft.getCameraEntity() != minecraft.player) return;
        float alpha = ClientInteractionManger.get().vignetteAlpha(deltaTracker.getGameTimeDeltaPartialTick(false));
        if (ClientInteractionManger.get().module().getItem() instanceof INvg nvg) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, nvg.getVignette());
            RenderSystem.setShaderColor(1, 1, 1, alpha);
            forgeGui.blit(nvg.getVignette(), 0, 0, forgeGui.guiWidth(), forgeGui.guiHeight(), 0, 0, forgeGui.guiWidth(), forgeGui.guiHeight(), forgeGui.guiWidth(), forgeGui.guiHeight());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    };

    @SubscribeEvent
    public static void onRegisterOverlay(RegisterGuiLayersEvent event) {
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "zc_duo_nvg_vignette"), DUO_VIGNETTE);
    }
}
