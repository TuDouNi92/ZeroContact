package net.zerocontact.api.armor.modular;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.armor.modular.model.MountDefinition;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public interface ModularEquipment {
    default Collection<MountDefinition> getMountDefinitions(ItemStack stack) {
        return Collections.emptyList();
    }

    default Optional<MountDefinition> getMountDefinition(ItemStack stack, ResourceLocation mountId) {
        return getMountDefinitions(stack)
                .stream()
                .filter(it -> it.mountId().equals(mountId))
                .findFirst();
    }

}
