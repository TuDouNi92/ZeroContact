package net.zerocontact.armor.modular.component;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Immutable component snapshot; live stacks belong exclusively to SimpleModuleContainer. */
public final class MountedModules {
    public static final Codec<MountedModules> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, ItemStack.CODEC)
            .xmap(MountedModules::new, MountedModules::copyModules);
    public static final MountedModules EMPTY = new MountedModules(Map.of());

    private final Map<ResourceLocation, ItemStack> modules;

    public MountedModules(Map<ResourceLocation, ItemStack> modules) {
        this.modules = copy(modules);
    }

    public Map<ResourceLocation, ItemStack> copyModules() {
        return copy(modules);
    }

    private static Map<ResourceLocation, ItemStack> copy(Map<ResourceLocation, ItemStack> source) {
        Map<ResourceLocation, ItemStack> result = new HashMap<>();
        source.forEach((id, stack) -> {
            if (!stack.isEmpty()) result.put(id, stack.copy());
        });
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MountedModules snapshot) || !modules.keySet().equals(snapshot.modules.keySet())) return false;
        return modules.entrySet().stream().allMatch(entry -> ItemStack.matches(entry.getValue(), snapshot.modules.get(entry.getKey())));
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (var entry : modules.entrySet()) {
            ItemStack stack = entry.getValue();
            hash += entry.getKey().hashCode() ^ (31 * ItemStack.hashItemAndComponents(stack) + stack.getCount());
        }
        return hash;
    }
}
