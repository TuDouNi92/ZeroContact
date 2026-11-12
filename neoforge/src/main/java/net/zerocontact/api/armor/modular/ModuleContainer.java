package net.zerocontact.api.armor.modular;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface ModuleContainer {
    // Returns the live mounted stack. Its component changes write through to the equipment.
    ItemStack getModule(ResourceLocation mountId);

    void setModule(ResourceLocation mountId, ItemStack module);

    ItemStack removeModule(ResourceLocation mountId);

    // Read-only map of live stacks; use setModule/removeModule to change the mounts.
    Map<ResourceLocation, ItemStack> getMountedModules();

    /** Replace a synchronized snapshot, including an empty one. */
    default void replaceModules(Map<ResourceLocation, ItemStack> modules) {
        Map<ResourceLocation, ItemStack> snapshot = new java.util.HashMap<>();
        modules.forEach((id, stack) -> snapshot.put(id, stack.copy()));
        new java.util.ArrayList<>(getMountedModules().keySet()).forEach(this::removeModule);
        snapshot.forEach(this::setModule);
    }
}
