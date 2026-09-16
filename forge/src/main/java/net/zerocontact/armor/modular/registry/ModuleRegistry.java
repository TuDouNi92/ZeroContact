package net.zerocontact.armor.modular.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.ModuleController;
import net.zerocontact.armor.modular.module.battery.capability.BatteryProvider;
import net.zerocontact.armor.modular.module.beacon.capability.BeaconProvider;
import net.zerocontact.armor.modular.module.beacon.service.BeaconController;
import net.zerocontact.armor.modular.module.headset.capability.HeadsetProvider;
import net.zerocontact.armor.modular.module.headset.service.HeadsetController;
import net.zerocontact.armor.modular.module.nvg.capability.NvgProvider;
import net.zerocontact.armor.modular.module.nvg.service.NvgController;
import net.zerocontact.armor.modular.module.pouch.capability.PouchProvider;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.model.MountDefinition;
import net.zerocontact.capability.CapabilityRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class ModuleRegistry {
    public static Optional<ModuleController> getController(ItemStack stack) {
        if (!(stack.getItem() instanceof net.zerocontact.api.armor.modular.EquipmentModule module)) {
            return Optional.empty();
        }
        CapabilityEntry trait = getTrait(module.getModuleTrait());
        return trait == null ? Optional.empty() : trait.controller();
    }

    public record CapabilityEntry(
            Capability<?> capability,
            Optional<ModuleController> controller,
            Supplier<? extends ICapabilityProvider> providerFactory
    ) {

    }

    //<ModuleResource,ModuleCategory>
    private static final Map<ResourceLocation, MountCategory> moduleItems = new HashMap<>();

    //<TraitId,Provider>
    private static final Map<ResourceLocation, CapabilityEntry> traits = new HashMap<>();

    public static void registerTrait() {
        traits.putAll(
                Map.of(
                        new ResourceLocation(ZeroContact.MOD_ID, "pouch"),
                        new CapabilityEntry(CapabilityRegistries.POUCH, Optional.empty(), PouchProvider::new),
                        new ResourceLocation(ZeroContact.MOD_ID, "nvg"),
                        new CapabilityEntry(CapabilityRegistries.NVG, Optional.of(new NvgController()), NvgProvider::new),
                        new ResourceLocation(ZeroContact.MOD_ID, "battery"),
                        new CapabilityEntry(CapabilityRegistries.BATTERY, Optional.empty(), BatteryProvider::new),
                        new ResourceLocation(ZeroContact.MOD_ID, "beacon"),
                        new CapabilityEntry(CapabilityRegistries.BEACON, Optional.of(new BeaconController()), BeaconProvider::new),
                        new ResourceLocation(ZeroContact.MOD_ID, "headset"),
                        new CapabilityEntry(CapabilityRegistries.HEADSET, Optional.of(new HeadsetController()), HeadsetProvider::new)
                )
        );
    }

    public static CapabilityEntry getTrait(ResourceLocation traitId) {
        return traits.get(traitId);
    }

    public static void registerCategory(ResourceLocation itemId, MountCategory category) {
        moduleItems.put(itemId, category);
    }

    public static MountCategory getCategory(ItemStack module) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(module.getItem());
        return moduleItems.getOrDefault(id, MountCategory.UNDEFINED);
    }

    /**
     * Returns one default stack per registered item accepted by this mount.
     */
    public static List<ItemStack> getModulesFor(MountDefinition mount) {
        return moduleItems.entrySet().stream()
                .filter(entry -> entry.getValue() != MountCategory.UNDEFINED)
                .filter(entry -> mount.acceptsModule(entry.getValue()))
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> ForgeRegistries.ITEMS.containsKey(entry.getKey()))
                .map(entry -> {
                    Item item = ForgeRegistries.ITEMS.getValue(entry.getKey());
                    if (item == null) return ItemStack.EMPTY;
                    return item.getDefaultInstance();
                })
                .filter(stack -> !stack.isEmpty())
                .toList();
    }
}
