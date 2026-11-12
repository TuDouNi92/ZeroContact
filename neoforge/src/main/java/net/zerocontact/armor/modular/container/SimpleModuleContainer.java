package net.zerocontact.armor.modular.container;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.armor.modular.ModularCapabilityAccess;
import net.zerocontact.api.armor.modular.ModuleContainer;
import net.zerocontact.armor.modular.component.ModularDataComponents;
import net.zerocontact.armor.modular.component.MountedModules;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SimpleModuleContainer implements ModuleContainer {
    private final Map<ResourceLocation, ItemStack> modules = new HashMap<>();
    private final ItemStack owner;
    private MountedModules snapshot;

    /** Detached container for packet snapshots. */
    public SimpleModuleContainer() {
        owner = null;
    }

    public SimpleModuleContainer(ItemStack owner) {
        this.owner = owner;
        refresh();
    }

    private void refresh() {
        if (owner == null) return;
        MountedModules current = owner.getOrDefault(ModularDataComponents.MOUNTED_MODULES.get(), MountedModules.EMPTY);
        if (snapshot == current) return;
        Map<ResourceLocation, ItemStack> incoming = current.copyModules();
        // Keep unchanged live objects, including navigation and radio runtime state.
        modules.forEach((id, stack) -> {
            if (ItemStack.matches(stack, incoming.getOrDefault(id, ItemStack.EMPTY))) incoming.put(id, stack);
            else detach(stack);
        });
        modules.clear();
        modules.putAll(incoming);
        snapshot = current;
        modules.forEach(this::attach);
    }

    private void attach(ResourceLocation id, ItemStack stack) {
        if (owner == null || stack.isEmpty()) return;
        ((ModularCapabilityAccess) (Object) stack).zeroContact$setModuleChangeListener(() -> {
            // A packet may have replaced the component since this stack was obtained.
            refresh();
            if (modules.get(id) == stack) commit();
        });
    }

    private static void detach(ItemStack stack) {
        if (!stack.isEmpty()) ((ModularCapabilityAccess) (Object) stack).zeroContact$setModuleChangeListener(null);
    }

    private void commit() {
        if (owner == null) return;
        MountedModules next = new MountedModules(modules);
        if (next.equals(snapshot)) return;
        snapshot = next;
        owner.set(ModularDataComponents.MOUNTED_MODULES.get(), next);
    }

    @Override
    public ItemStack getModule(ResourceLocation mountId) {
        refresh();
        return modules.getOrDefault(mountId, ItemStack.EMPTY);
    }

    @Override
    public void setModule(ResourceLocation mountId, ItemStack module) {
        refresh();
        if (module.isEmpty()) {
            removeModule(mountId);
            return;
        }
        if (modules.get(mountId) == module) {
            commit();
            return;
        }
        ItemStack copy = module.copyWithCount(1);
        ItemStack previous = modules.put(mountId, copy);
        if (previous != null) detach(previous);
        attach(mountId, copy);
        commit();
    }

    @Override
    public ItemStack removeModule(ResourceLocation mountId) {
        refresh();
        ItemStack removed = modules.remove(mountId);
        if (removed == null) return ItemStack.EMPTY;
        detach(removed);
        commit();
        return removed;
    }

    @Override
    public Map<ResourceLocation, ItemStack> getMountedModules() {
        refresh();
        return Collections.unmodifiableMap(modules);
    }

    @Override
    public void replaceModules(Map<ResourceLocation, ItemStack> replacement) {
        MountedModules incoming = new MountedModules(replacement);
        if (owner != null) {
            owner.set(ModularDataComponents.MOUNTED_MODULES.get(), incoming);
            refresh();
        } else {
            modules.values().forEach(SimpleModuleContainer::detach);
            modules.clear();
            modules.putAll(incoming.copyModules());
        }
    }

    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        refresh();
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        modules.forEach((id, stack) -> {
            if (stack.isEmpty()) return;
            CompoundTag entry = new CompoundTag();
            entry.putString("Mount", id.toString());
            entry.put("Stack", stack.save(registries));
            list.add(entry);
        });
        root.put("Modules", list);
        return root;
    }

    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag root) {
        refresh();
        modules.values().forEach(SimpleModuleContainer::detach);
        modules.clear();
        ListTag list = root.getList("Modules", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(entry.getString("Mount"));
            if (id == null || !entry.contains("Stack", Tag.TAG_COMPOUND)) continue;
            ItemStack stack = ItemStack.parseOptional(registries, entry.getCompound("Stack"));
            if (!stack.isEmpty()) modules.put(id, stack);
        }
        modules.forEach(this::attach);
        commit();
    }
}
