package net.zerocontact.armor.modular.module.radio.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.client.interaction.KeyBindingHandler;

@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
public final class RadioScreenInputHandler {
    private RadioScreenInputHandler() {}

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        if (KeyBindingHandler.OPEN_RADIO == null) return;
        while (KeyBindingHandler.OPEN_RADIO.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen == null && minecraft.player != null) {
                RadioScreen.openFirstActive(minecraft);
            }
        }
    }
}
