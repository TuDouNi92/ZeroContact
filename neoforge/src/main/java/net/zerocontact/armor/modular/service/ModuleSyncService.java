package net.zerocontact.armor.modular.service;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.armor.modular.capability.ModularCapabilityRegistry;
import net.zerocontact.armor.modular.client.network.s2c.SyncModulesPacket;
import net.zerocontact.armor.modular.container.SimpleModuleContainer;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.network.ModMessages;

import java.util.Optional;

public final class ModuleSyncService {
    private ModuleSyncService() {}

    /** Call after modifying modules on an equipped stack, on the server. */
    public static void sync(Player wearer, EquipmentSlot slot) {
        sync(wearer, new EquipmentTarget("armor/" + slot.getName(), 0));
    }

    public static void sync(Player wearer, EquipmentTarget target) {
        if (wearer.level().isClientSide()) return;
        ItemStack equipment = target.resolve(wearer);
        if (equipment.isEmpty()) return;
        Optional.ofNullable(equipment.getCapability(ModularCapabilityRegistry.MODULAR_EQUIPMENT)).ifPresent(container -> {
            // GeckoLib assigns animation IDs directly on the component map.
            new java.util.ArrayList<>(container.getMountedModules().entrySet())
                    .forEach(entry -> container.setModule(entry.getKey(), entry.getValue()));
            SimpleModuleContainer snapshot = new SimpleModuleContainer();
            container.getMountedModules().forEach((id, stack) -> snapshot.setModule(id, stack.copy()));
            ModMessages.sendToTrackingAndSelf(new SyncModulesPacket(wearer.getId(), target,
                    BuiltInRegistries.ITEM.getKey(equipment.getItem()), snapshot.serializeNBT(wearer.registryAccess())), wearer);
        });
    }
}
