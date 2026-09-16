package net.zerocontact.armor.modular.module.headset.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.module.headset.service.HeadsetManager;

/** Publishes the client-thread equipment state to the sound thread. */
@Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientHeadsetState {
    private static volatile boolean active;

    private ClientHeadsetState() {
    }

    /** Safe on the sound thread: never accesses the player or equipment capabilities. */
    public static boolean isActive() {
        return active;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        active = minecraft.level != null && HeadsetManager.isActive(minecraft.player);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        active = false;
    }
}
