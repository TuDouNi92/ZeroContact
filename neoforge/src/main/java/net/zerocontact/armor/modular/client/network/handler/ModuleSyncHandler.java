package net.zerocontact.armor.modular.client.network.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.armor.modular.capability.ModularCapabilityRegistry;
import net.zerocontact.armor.modular.client.network.s2c.SyncModulesPacket;
import net.zerocontact.armor.modular.container.SimpleModuleContainer;

import java.util.Optional;

public final class ModuleSyncHandler {
    private ModuleSyncHandler() {}

    public static void handle(SyncModulesPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null || !(level.getEntity(packet.entityId()) instanceof Player entity)) return;
        ItemStack equipment = packet.target().resolve(entity);
        if (equipment.isEmpty() || !packet.itemId().equals(BuiltInRegistries.ITEM.getKey(equipment.getItem()))) return;

        Optional.ofNullable(equipment.getCapability(ModularCapabilityRegistry.MODULAR_EQUIPMENT)).ifPresent(container -> {
            SimpleModuleContainer snapshot = new SimpleModuleContainer();
            snapshot.deserializeNBT(level.registryAccess(), packet.modules());
            // Replace, rather than merge, so removed modules disappear on the client.
            container.replaceModules(snapshot.getMountedModules());
        });
    }
}
