package net.zerocontact.armor.modular.module.nvg.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.armor.modular.module.nvg.client.ClientInteractionManger;
import net.zerocontact.mixin.minecraft.GameRendererAccessor;

import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientNvgTickHandler {
    private static final Map<INvg.Type, ResourceLocation> TYPE_EFFECTS = Map.of(
            INvg.Type.GREEN, new ResourceLocation(ZeroContact.MOD_ID, "shaders/post/nvg_green.json"),
            INvg.Type.WHITE, new ResourceLocation(ZeroContact.MOD_ID, "shaders/post/nvg_white.json")
    );
    private static ResourceLocation loadedEffect;
    private static ResourceLocation failedEffect;
    private static PostChain ownedEffect;
    private static boolean reloadPending;

    @Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ReloadListener {
        @SubscribeEvent
        public static void register(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) resources -> reloadPending = true);
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getOverlay() instanceof LoadingOverlay) return;
        var effect = ClientInteractionManger.get();
        if (!minecraft.isPaused()) effect.tick(minecraft);
        GameRenderer renderer = minecraft.gameRenderer;
        ResourceLocation desired =
                effect.wantsShader()
                && effect.module().getItem() instanceof INvg nvg
                && minecraft.options.getCameraType().isFirstPerson()
                ? TYPE_EFFECTS.get(nvg.getNVGType()) : null;

        if (reloadPending) {
            release(renderer);
            failedEffect = null;
            reloadPending = false;
        }
        if (ownedEffect != null && renderer.currentEffect() != ownedEffect) {
            // Another system replaced our chain. Never close the replacement.
            ownedEffect = null;
            loadedEffect = null;
        }
        if (!Objects.equals(desired, loadedEffect)) release(renderer);
        if (desired == null) {
            failedEffect = null;
            return;
        }
        if (ownedEffect != null && renderer.currentEffect() == ownedEffect) {
            if(!((GameRendererAccessor) renderer).isEffectActive()){
                renderer.togglePostEffect();
            }
            return;
        }
        if (ownedEffect != null || renderer.currentEffect() != null) return;
        if (Objects.equals(failedEffect, desired)) return; // Retry on toggle or resource reload.
        renderer.loadEffect(desired);
        ownedEffect = renderer.currentEffect();
        if (ownedEffect == null) failedEffect = desired;
        else {
            loadedEffect = desired;
            failedEffect = null;
        }
    }

    private static void release(GameRenderer renderer) {
        if (ownedEffect != null && renderer.currentEffect() == ownedEffect) renderer.shutdownEffect();
        ownedEffect = null;
        loadedEffect = null;
    }
}
