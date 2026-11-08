package net.zerocontact.api.armor.modular;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface ModuleContainer {
    //get installed module by mountId.
    ItemStack getModule(ResourceLocation mountId);

    void setModule(ResourceLocation mountId, ItemStack module);

    ItemStack removeModule(ResourceLocation mountId);

    //get modules from all mounts
    Map<ResourceLocation, ItemStack> getMountedModules();
}
