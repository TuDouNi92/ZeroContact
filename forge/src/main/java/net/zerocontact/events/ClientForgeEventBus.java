package net.zerocontact.events;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.ClientData;
import net.zerocontact.client.interaction.BulletPassBy;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientForgeEventBus {
    private static boolean lastPressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) return;
        listenVisorKey();
        listenBackpackKey();
    }
    @SubscribeEvent
    public static void clientEntityTick(EntityEvent event){
        playBulletSound(event);
    }
    private static void playBulletSound(EntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityKineticBullet) {
            BulletPassBy.playBulletPassBySound(entity, Minecraft.getInstance().player);
        }
    }

    private static void listenBackpackKey() {
        while (KeyBindingHandler.TOGGLE_BACKPACK_KEY.consumeClick()) {
            if (Minecraft.getInstance().screen == null && !ClientData.justCloseBackpack) {
                ModMessages.sendToServer(new NetworkHandler.ToggleBackpackPacket(true));
            }
        }
        ClientData.justCloseBackpack = false;
    }

    private static void listenVisorKey() {
        if (toggle(KeyBindingHandler.TOGGLE_VISOR_KEY.isDown())) {
            ClientData.setTriggerViosorToggle(true);
            ModMessages.sendToServer(new NetworkHandler.ToggleVisorPacket(ClientData.isTriggerVisorToggle()));
        } else {
            ClientData.setTriggerViosorToggle(false);
        }
    }

    public static boolean toggle(boolean keyPressedDown) {
        boolean canToggle = keyPressedDown && !lastPressed;
        lastPressed = keyPressedDown;
        return canToggle;
    }
}
