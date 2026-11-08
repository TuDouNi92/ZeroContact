package net.zerocontact.armor.modular.client.network.handler;

import net.minecraft.client.Minecraft;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu;
import net.zerocontact.armor.modular.client.network.s2c.SyncEquipmentCandidatesPacket;

public final class EquipmentCandidatesHandler {
    private EquipmentCandidatesHandler() {}

    public static void handle(SyncEquipmentCandidatesPacket packet) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof EquipmentMenu menu
                && menu.containerId == packet.containerId()) {
            menu.applyCandidates(packet.target(), packet.equipment(), packet.mountId(), packet.candidates());
        }
    }
}
