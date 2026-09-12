package net.zerocontact.armor.modular.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.module.nvg.client.ClientInteractionManger;
import net.zerocontact.armor.modular.module.nvg.client.NvgEffectState;

/**
 * Camera-relative view model, independent of the player's held items and NVG power.
 */
@Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FirstPersonNvgRenderer {
    // Camera coordinates: +X right, +Y up, -Z forward. Kept together for model fitting.
    private static final double OFFSET_X = 0.0;
    private static final double OFFSET_Y = 0.3;
    private static final double OFFSET_Z = -0.35;
    private static final float SCALE = 1.74F;
    private static boolean renderedThisFrame;

    private FirstPersonNvgRenderer() {
    }

    @SubscribeEvent
    public static void beginFrame(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) renderedThisFrame = false;
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void render(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (renderedThisFrame || minecraft.player == null || minecraft.level == null
                || !minecraft.options.getCameraType().isFirstPerson()
                || minecraft.getCameraEntity() != minecraft.player
                || minecraft.player.isSpectator() || !minecraft.player.isAlive()
                || minecraft.options.hideGui) return;

        var effect = ClientInteractionManger.get();
        ItemStack module = effect.module();
        if (module.isEmpty()) return;
        renderedThisFrame = true;

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        try {
            // RenderHandEvent precedes per-hand swing/equip transforms. Do not cancel the hands.
            pose.translate(OFFSET_X, OFFSET_Y, OFFSET_Z);
            pose.scale(SCALE, SCALE, SCALE);
            // GeoItemRenderer adds this item-space origin in preRender; center the model on the camera.
            pose.translate(-0.5, -0.51, -0.5);
            effect.beginRender();
            if (effect.phase() == NvgEffectState.Phase.ON) return;
            IClientItemExtensions.of(module.getItem()).getCustomRenderer().renderByItem(
                    module, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, pose,
                    event.getMultiBufferSource(), event.getPackedLight(), OverlayTexture.NO_OVERLAY);
        } finally {
            effect.endRender();
            pose.popPose();
        }
    }
}
