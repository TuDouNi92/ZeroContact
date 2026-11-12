package net.zerocontact.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.zerocontact.client.ClientData;
import net.zerocontact.client.animation.VisorTracker;
import net.zerocontact.client.interaction.SuppressionManager;
import net.zerocontact.network.s2c.AppendSuppressionPacket;
import net.zerocontact.network.s2c.ToggleVisorResultPacket;
import net.zerocontact.registries.ModSoundEventsReg;

/** Client game code stays outside payload classes registered on both physical sides. */
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {}

    public static void syncStamina(float stamina, boolean enabled) {
        ClientData.setStamina(stamina, enabled);
    }

    public static void toggleVisor(ToggleVisorResultPacket packet) {
        VisorTracker.update(packet.visorAnimateData());
    }

    public static void appendSuppression(AppendSuppressionPacket packet) {
        SuppressionManager.increaseSuppression(packet.amount(), () -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                minecraft.level.playLocalSound(
                        packet.bulletPos().x(),
                        packet.bulletPos().y(),
                        packet.bulletPos().z(),
                        ModSoundEventsReg.randomBulletSound(),
                        SoundSource.HOSTILE,
                        2.0F,
                        1.0F,
                        false
                );
            }
        });
    }
}
