package net.zerocontact.armor.modular.module.headset.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.module.headset.model.AudioProfile;
import net.zerocontact.armor.modular.module.headset.service.HeadsetService;
import org.jetbrains.annotations.Nullable;

/**
 * Publishes the client-thread equipment state to the sound thread.
 */
@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
public final class HeadsetAudioState {
    private static volatile boolean active;
    private static AudioProfile profile = null;

    private HeadsetAudioState() {
    }

    /**
     * Safe on the sound thread: never accesses the player or equipment capabilities.
     */
    public static boolean isActive() {
        return active;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
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

    public static @Nullable AudioProfile getProfile() {
        return profile;
    }
}
