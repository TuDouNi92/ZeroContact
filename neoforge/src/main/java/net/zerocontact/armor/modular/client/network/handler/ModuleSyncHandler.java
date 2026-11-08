package net.zerocontact.armor.modular.client.network.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.armor.modular.container.SimpleModuleContainer;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.armor.modular.client.network.s2c.SyncModulesPacket;

import java.util.ArrayList;

public final class ModuleSyncHandler {
    private ModuleSyncHandler() {}

    public static void handle(SyncModulesPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null || !(level.getEntity(packet.entityId()) instanceof Player entity)) return;
        ItemStack equipment = packet.target().resolve(entity);
        if (equipment.isEmpty() || !packet.itemId().equals(ForgeRegistries.ITEMS.getKey(equipment.getItem()))) return;

        equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT).ifPresent(container -> {
            SimpleModuleContainer snapshot = new SimpleModuleContainer();
            snapshot.deserializeNBT(packet.modules());
            // Replace, rather than merge, so removed modules disappear on the client.
            new ArrayList<>(container.getMountedModules().keySet()).forEach(container::removeModule);
            snapshot.getMountedModules().forEach(container::setModule);
        });
    }
}
