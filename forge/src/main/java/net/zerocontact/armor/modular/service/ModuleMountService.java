package net.zerocontact.armor.modular.service;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.armor.modular.ModularEquipment;
import net.zerocontact.api.armor.modular.ModuleContainer;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.capability.CapabilityRegistries;

public final class ModuleMountService {

    public static boolean canMount(
            ItemStack equipment,
            ResourceLocation mountId,
            ItemStack module
    ) {
        if (!(equipment.getItem() instanceof ModularEquipment modular)) {
            return false;
        }

        MountCategory category =
                ModuleRegistry.getCategory(module);

        return modular.getMountDefinition(equipment, mountId)
                .map(def -> def.acceptsModule(category))
                .orElse(false);
    }

    public static boolean mount(ItemStack equipment, ResourceLocation mountId, ItemStack module) {

        boolean canMount = canMount(equipment, mountId, module);
        ModuleContainer moduleContainer = equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT)
                .map(cap -> cap)
                .orElse(null);

        if (moduleContainer == null) return false;
        if (!canMount) return false;

        moduleContainer.setModule(mountId, module);
        module.shrink(1);
        return true;
    }

    public static ItemStack unMount(ItemStack equipment, ResourceLocation mountId) {
        if (!(equipment.getItem() instanceof ModularEquipment)) {
            return ItemStack.EMPTY;
        }
        ModuleContainer moduleContainer = equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT)
                .map(cap -> cap)
                .orElse(null);

        if (moduleContainer == null) return ItemStack.EMPTY;

        return moduleContainer.removeModule(mountId);
    }

    public static ItemStack swap(ItemStack equipment, ResourceLocation mountId, ItemStack newModule) {

        boolean canMount = canMount(equipment, mountId, newModule);

        if (!canMount) return ItemStack.EMPTY;

        ModuleContainer moduleContainer = equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT)
                .map(cap -> cap)
                .orElse(null);

        if (moduleContainer == null) return ItemStack.EMPTY;

        ItemStack oldModule = moduleContainer.removeModule(mountId);

        moduleContainer.setModule(mountId, newModule);

        return oldModule;

    }

}
