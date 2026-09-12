package net.zerocontact.armor.modular.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.zerocontact.api.armor.modular.ModuleContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SimpleModuleContainer implements ModuleContainer, INBTSerializable<CompoundTag> {

    private final Map<ResourceLocation, ItemStack> modules =
            new HashMap<>();

    @Override
    public ItemStack getModule(ResourceLocation mountId) {
        return modules.getOrDefault(mountId, ItemStack.EMPTY);
    }

    @Override
    public void setModule(ResourceLocation mountId, ItemStack module) {
        if (module.isEmpty()) return;
        modules.put(mountId, module.copy());
    }

    @Override
    public ItemStack removeModule(ResourceLocation mountId) {
        ItemStack removed = modules.remove(mountId);
        return removed == null ? ItemStack.EMPTY : removed;
    }

    @Override
    public Map<ResourceLocation, ItemStack> getMountedModules() {
        return Collections.unmodifiableMap(modules);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        ListTag modulesTag = new ListTag();

        for (Map.Entry<ResourceLocation, ItemStack> entry
                : this.getMountedModules().entrySet()) {

            ItemStack stack = entry.getValue();
            if (stack.isEmpty()) {
                continue;
            }

            CompoundTag moduleTag = new CompoundTag();

            moduleTag.putString("Mount", entry.getKey().toString());

            CompoundTag stackTag = new CompoundTag();
            stack.save(stackTag);
            moduleTag.put("Stack", stackTag);

            modulesTag.add(moduleTag);
        }

        root.put("Modules", modulesTag);
        return root;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        this.modules.clear();

        ListTag modulesTag = arg.getList("Modules", Tag.TAG_COMPOUND);

        for (int i = 0; i < modulesTag.size(); i++) {
            CompoundTag moduleTag = modulesTag.getCompound(i);

            if (!moduleTag.contains("Mount", Tag.TAG_STRING)
                    || !moduleTag.contains("Stack", Tag.TAG_COMPOUND)) {
                continue;
            }

            ResourceLocation mountId =
                    ResourceLocation.tryParse(moduleTag.getString("Mount"));

            if (mountId == null) {
                continue;
            }

            ItemStack stack =
                    ItemStack.of(moduleTag.getCompound("Stack"));

            if (!stack.isEmpty()) {
                this.setModule(mountId, stack);
            }
        }
    }
}
