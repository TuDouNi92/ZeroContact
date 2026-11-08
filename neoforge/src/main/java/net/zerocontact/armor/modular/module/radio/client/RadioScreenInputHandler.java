package net.zerocontact.armor.modular.module.radio.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.interaction.KeyBindingHandler;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RadioScreenInputHandler {
    private RadioScreenInputHandler() {}

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || KeyBindingHandler.OPEN_RADIO == null) return;
        while (KeyBindingHandler.OPEN_RADIO.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen == null && minecraft.player != null) {
                RadioScreen.openFirstActive(minecraft);
            }
        }
    }
}
