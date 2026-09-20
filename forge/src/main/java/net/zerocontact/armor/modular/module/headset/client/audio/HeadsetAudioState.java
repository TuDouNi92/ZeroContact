package net.zerocontact.armor.modular.module.headset.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.module.headset.item.Headset;
import net.zerocontact.armor.modular.module.headset.service.HeadsetService;
import org.jetbrains.annotations.Nullable;

/**
 * Publishes the client-thread equipment state to the sound thread.
 */
@Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class HeadsetAudioState {
    private static volatile boolean active;
    private static Headset.AudioProfile profile = null;

    private HeadsetAudioState() {
    }

    /**
     * Safe on the sound thread: never accesses the player or equipment capabilities.
     */
    public static boolean isActive() {
        return active;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        active = level != null && HeadsetService.isActive(minecraft.player);
        if (level != null) {
            profile = HeadsetService.getAudioProfile(minecraft.player).orElse(null);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        active = false;
        profile = null;
    }

    public static @Nullable Headset.AudioProfile getProfile() {
        return profile;
    }
}
