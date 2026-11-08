package net.zerocontact.armor.modular.module.nvg.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.armor.modular.module.nvg.client.ClientInteractionManger;

@Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DuoVignetteOverlay {
    public static final IGuiOverlay DUO_VIGNETTE = (forgeGui, graphics, partialTick, w, h) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.isSpectator()
                || minecraft.options.hideGui || !minecraft.options.getCameraType().isFirstPerson()
                || minecraft.getCameraEntity() != minecraft.player) return;
        float alpha = ClientInteractionManger.get().vignetteAlpha(partialTick);
        if (ClientInteractionManger.get().module().getItem() instanceof INvg nvg) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, nvg.getVignette());
            RenderSystem.setShaderColor(1, 1, 1, alpha);
            graphics.blit(nvg.getVignette(), 0, 0, w, h, 0, 0, w, h, w, h);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    };

    @SubscribeEvent
    public static void onRegisterOverlay(RegisterGuiOverlaysEvent event) {
        event.registerBelowAll("zc_duo_nvg_vignette", DUO_VIGNETTE);
    }
}
