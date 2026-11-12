package net.zerocontact.armor.modular.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.ModuleController;
import net.zerocontact.api.armor.modular.ModuleUseHandler;
import net.zerocontact.armor.modular.capability.ModularCapabilityRegistry;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.model.MountDefinition;
import net.zerocontact.armor.modular.module.battery.container.BatteryContainer;
import net.zerocontact.armor.modular.module.beacon.container.BeaconContainer;
import net.zerocontact.armor.modular.module.beacon.service.BeaconController;
import net.zerocontact.armor.modular.module.headset.container.HeadsetContainer;
import net.zerocontact.armor.modular.module.headset.service.HeadsetController;
import net.zerocontact.armor.modular.module.nvg.container.NvgContainer;
import net.zerocontact.armor.modular.module.nvg.service.NvgController;
import net.zerocontact.armor.modular.module.pouch.container.AdminPouchContainer;
import net.zerocontact.armor.modular.module.pouch.container.PouchContainer;
import net.zerocontact.armor.modular.module.pouch.container.navboard.NavBoardContainer;
import net.zerocontact.armor.modular.module.pouch.service.AdminPouchController;
import net.zerocontact.armor.modular.module.pouch.service.NavBoardController;
import net.zerocontact.armor.modular.module.radio.container.RadioContainer;
import net.zerocontact.armor.modular.module.radio.service.MBITRController;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class ModuleRegistry {
    public static Optional<ModuleController> getController(ItemStack stack) {
        if (!(stack.getItem() instanceof net.zerocontact.api.armor.modular.EquipmentModule module)) {
            return Optional.empty();
        }
        CapabilityEntry<?> trait = getTrait(module.getModuleTrait());
        return trait == null ? Optional.empty() : Optional.ofNullable(trait.controller());
    }

    public record CapabilityEntry<T>(
            ItemCapability<T, Void> capability,
            ModuleController controller,
            Function<ItemStack, T> containerFactory,
            Optional<ModuleUseHandler> useHandler
    ) {
        public CapabilityEntry(ItemCapability<T, Void> capability, @Nullable ModuleController controller,
                               Function<ItemStack, T> containerFactory) {
            this(capability, controller, containerFactory, Optional.empty());
        }

        public CapabilityEntry<T> withUse(ModuleUseHandler handler) {
            return new CapabilityEntry<>(capability, controller, containerFactory, Optional.of(handler));
        }
    }

    //<ModuleResource,ModuleCategory>
    private static final Map<ResourceLocation, MountCategory> moduleItems = new HashMap<>();

    // Trait factories are stack-bound; registration and controller lookup share this table.
    private static final Map<ResourceLocation, CapabilityEntry<?>> traits = new HashMap<>();

    public static void registerTrait() {
        traits.putAll(
                Map.of(
                        ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "pouch"),
                        new CapabilityEntry<>(ModularCapabilityRegistry.POUCH, null, stack -> new PouchContainer()),
                        ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "admin_pouch"),
                        new CapabilityEntry<>(ModularCapabilityRegistry.ADMIN_POUCH, new AdminPouchController(), AdminPouchContainer::new),
                        ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "navboard"),
                        new CapabilityEntry<>(ModularCapabilityRegistry.NAV_BOARD, new NavBoardController(), NavBoardContainer::new),
                        ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "nvg"),
                        new CapabilityEntry<>(ModularCapabilityRegistry.NVG, new NvgController(), NvgContainer::new),
                        ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "battery"),
                        new CapabilityEntry<>(ModularCapabilityRegistry.BATTERY, null, BatteryContainer::new),
                        ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "beacon"),
                        new CapabilityEntry<>(ModularCapabilityRegistry.BEACON, new BeaconController(), BeaconContainer::new),
                        ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "headset"),
                        new CapabilityEntry<>(ModularCapabilityRegistry.HEADSET, new HeadsetController(), HeadsetContainer::new),
                        ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "radio"),
                        new CapabilityEntry<>(ModularCapabilityRegistry.RADIO, new MBITRController(), RadioContainer::new)

                )
        );
    }

    public static CapabilityEntry<?> getTrait(ResourceLocation traitId) {
        return traits.get(traitId);
    }

    public static void registerCategory(ResourceLocation itemId, MountCategory category) {
        moduleItems.put(itemId, category);
    }

    public static MountCategory getCategory(ItemStack module) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(module.getItem());
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
                .filter(entry -> BuiltInRegistries.ITEM.containsKey(entry.getKey()))
                .map(entry -> {
                    Item item = BuiltInRegistries.ITEM.get(entry.getKey());
                    if (item == null) return ItemStack.EMPTY;
                    return item.getDefaultInstance();
                })
                .filter(stack -> !stack.isEmpty())
                .toList();
    }
}
