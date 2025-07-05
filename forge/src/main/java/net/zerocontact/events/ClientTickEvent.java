package net.zerocontact.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.ClientData;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTickEvent {
    private static boolean lastPressed = false;
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) return;
        boolean pressed = KeyBindingHandler.TOGGLE_VISOR_KEY.isDown();
        if (pressed && !lastPressed) {
            ClientData.setTriggerToggle(true);
            ModMessages.sendToServer(new NetworkHandler.ToggleVisorPacket(ClientData.isTriggerToggle()));
        } else {
            ClientData.setTriggerToggle(false);
        }
        lastPressed = pressed;
    }

}
