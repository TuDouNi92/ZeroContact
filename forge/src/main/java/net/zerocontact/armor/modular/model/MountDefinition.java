package net.zerocontact.armor.modular.model;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public record MountDefinition(
        ResourceLocation mountId,
        MountType type,
        String mountBone,
        Set<MountCategory> acceptedModuleCategories
) {
    public MountDefinition {
        acceptedModuleCategories = Set.copyOf(acceptedModuleCategories);
    }

    public boolean acceptsModule(MountCategory category) {
        return acceptedModuleCategories.contains(category);
    }
}
