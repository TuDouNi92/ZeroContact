package net.zerocontact.armor.modular.service;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.armor.modular.container.SimpleModuleContainer;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.network.ModMessages;
import net.zerocontact.armor.modular.client.network.s2c.SyncModulesPacket;

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
        equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT).ifPresent(container -> {
            SimpleModuleContainer snapshot = new SimpleModuleContainer();
            container.getMountedModules().forEach((id, stack) -> snapshot.setModule(id, stack.copy()));
            ModMessages.sendToTrackingAndSelf(new SyncModulesPacket(wearer.getId(), target,
                    ForgeRegistries.ITEMS.getKey(equipment.getItem()), snapshot.serializeNBT()), wearer);
        });
    }
}
