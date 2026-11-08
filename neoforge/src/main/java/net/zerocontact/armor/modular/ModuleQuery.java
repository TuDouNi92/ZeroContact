package net.zerocontact.armor.modular;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.Optional;
import java.util.stream.Stream;

public final class ModuleQuery {

    public record MountedModuleRef(
            EquipmentTarget equipmentTarget,
            ResourceLocation mountId,
            ItemStack stack
    ) {
    }

    public static Optional<ItemStack> getMounted(
            ItemStack equipment,
            ResourceLocation mountId
    ) {
        return equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT)
                .map(cap -> Optional.of(cap.getModule(mountId)))
                .orElse(Optional.empty());
    }

    public static Stream<ItemStack> streamMounted(ItemStack equipment) {
        return equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT)
                .map(cap -> cap.getMountedModules().values().stream())
                .orElse(Stream.empty());
    }

    public static Stream<MountedModuleRef> streamMounted(
            Player wearer
    ) {
        return EquipmentTarget.wornBy(wearer).entrySet()
                .stream()
                .flatMap(entry -> {
                    EquipmentTarget target = entry.getKey();
                    ItemStack equipment = entry.getValue();
                    return equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT)
                            .map(cap -> cap.getMountedModules()
                                    .entrySet()
                                    .stream()
                                    .map(entry1 -> new MountedModuleRef(
                                            target,
                                            entry1.getKey(),
                                            entry1.getValue()
                                    ))).orElse(Stream.empty());
                });
    }
}
