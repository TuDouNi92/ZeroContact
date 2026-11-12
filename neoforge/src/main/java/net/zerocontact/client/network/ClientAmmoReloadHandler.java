package net.zerocontact.client.network;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.capability.CapabilityRegistry;
import net.zerocontact.network.s2c.ClientAmmoReloadPacket;

import java.util.Optional;

public final class ClientAmmoReloadHandler {
    private ClientAmmoReloadHandler() {
    }

    public static void handle(ClientAmmoReloadPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        int gunSlot = packet.gunSlot();
        Inventory inventory = player.getInventory();
        if (gunSlot < 0 || gunSlot >= inventory.items.size()) return;
        if (inventory.selected != gunSlot) return;
        if (!IGun.mainHandHoldGun(player)) return;
        ItemStack gunStack = player.getMainHandItem();
        Optional.ofNullable(gunStack.getCapability(CapabilityRegistry.CARTRIDGE)).ifPresent(cap -> {
            cap.setClientSelectedAmmoVariant(gunStack, packet.selectedAmmoKey());
            IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
            operator.reload();
        });
    }
}
