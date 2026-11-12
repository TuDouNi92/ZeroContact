package net.zerocontact.armor.modular.module.nvg.event;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.module.nvg.api.INvg;
import net.zerocontact.armor.modular.module.nvg.client.ClientInteractionManger;
import net.zerocontact.mixin.minecraft.GameRendererAccessor;
import net.zerocontact.mixin.minecraft.PostChainAccessor;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
public final class ClientNvgTickHandler {
    private static final Map<INvg.Type, ResourceLocation> TYPE_EFFECTS = Map.of(
            INvg.Type.GREEN, ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "shaders/post/nvg_green.json"),
            INvg.Type.WHITE, ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "shaders/post/nvg_white.json"),
            INvg.Type.THERMAL, ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "shaders/post/thermal_white.json"),
            INvg.Type.THERMAL_COLOR, ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "shaders/post/thermal_color.json")
    );
    private static ResourceLocation loadedEffect;
    private static ResourceLocation failedEffect;
    private static PostChain ownedEffect;
    private static boolean reloadPending;

    @EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
    public static final class ReloadListener {
        @SubscribeEvent
        public static void register(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) resources -> reloadPending = true);
        }
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
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

    @SubscribeEvent
    public static void renderTick(RenderFrameEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null
                || minecraft.getOverlay() instanceof LoadingOverlay || reloadPending) return;
        GameRenderer renderer = minecraft.gameRenderer;
        if (ownedEffect == null || renderer.currentEffect() != ownedEffect
                || !((GameRendererAccessor) renderer).isEffectActive()) return;
        if (!Objects.equals(loadedEffect, TYPE_EFFECTS.get(INvg.Type.GREEN))
                && !Objects.equals(loadedEffect, TYPE_EFFECTS.get(INvg.Type.WHITE))) return;

        // Update the existing program before this frame's PostChain.process().
        float partialTick = minecraft.isPaused() ? 1.0F : event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float gain = ClientInteractionManger.get().gain(partialTick);
        for (var pass : ((PostChainAccessor) ownedEffect).zerocontact$getPasses()) {
            var uniform = pass.getEffect().getUniform("Gain");
            if (uniform != null) uniform.set(gain);
        }
    }

    /** Only expose a target owned by the currently active thermal chain. */
    @Nullable
    static RenderTarget getThermalTarget() {
        Minecraft minecraft = Minecraft.getInstance();
        GameRenderer renderer = minecraft.gameRenderer;
        if (ownedEffect == null || renderer.currentEffect() != ownedEffect
                || !((GameRendererAccessor) renderer).isEffectActive()) return null;
        if (!Objects.equals(loadedEffect, TYPE_EFFECTS.get(INvg.Type.THERMAL))
                && !Objects.equals(loadedEffect, TYPE_EFFECTS.get(INvg.Type.THERMAL_COLOR))) return null;
        return ownedEffect.getTempTarget("heat");
    }

    @Nullable
    static RenderTarget getThermalWorldDepthTarget() {
        return getThermalTarget() == null ? null : ownedEffect.getTempTarget("world_depth");
    }

    private static void release(GameRenderer renderer) {
        if (ownedEffect != null && renderer.currentEffect() == ownedEffect) renderer.shutdownEffect();
        ownedEffect = null;
        loadedEffect = null;
    }
}
