package net.zerocontact.armor.modular.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.ModuleController;
import net.zerocontact.api.armor.modular.ModuleUseHandler;
import net.zerocontact.armor.modular.module.battery.capability.BatteryProvider;
import net.zerocontact.armor.modular.module.beacon.capability.BeaconProvider;
import net.zerocontact.armor.modular.module.beacon.service.BeaconController;
import net.zerocontact.armor.modular.module.headset.capability.HeadsetProvider;
import net.zerocontact.armor.modular.module.headset.service.HeadsetController;
import net.zerocontact.armor.modular.module.nvg.capability.NvgProvider;
import net.zerocontact.armor.modular.module.nvg.service.NvgController;
import net.zerocontact.armor.modular.module.pouch.capability.AdminPouchProvider;
import net.zerocontact.armor.modular.module.pouch.capability.PouchProvider;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.model.MountDefinition;
import net.zerocontact.armor.modular.module.pouch.service.AdminPouchController;
import net.zerocontact.armor.modular.module.pouch.service.NavBoardController;
import net.zerocontact.armor.modular.module.pouch.capability.NavBoardProvider;
import net.zerocontact.armor.modular.module.radio.capability.RadioProvider;
import net.zerocontact.armor.modular.module.radio.service.MBITRController;
import net.zerocontact.capability.CapabilityRegistries;
import org.jetbrains.annotations.Nullable;

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
        return trait == null ? Optional.empty() : Optional.ofNullable(trait.controller());
    }

    public record CapabilityEntry(
            Capability<?> capability,
            ModuleController controller,
            Supplier<? extends ICapabilityProvider> providerFactory,
            Optional<ModuleUseHandler> useHandler
    ) {
        public CapabilityEntry(Capability<?> capability, @Nullable ModuleController controller,
                               Supplier<? extends ICapabilityProvider> providerFactory) {
            this(capability, controller, providerFactory, Optional.empty());
        }

        public CapabilityEntry withUse(ModuleUseHandler handler) {
            return new CapabilityEntry(capability, controller, providerFactory, Optional.of(handler));
        }
    }

    //<ModuleResource,ModuleCategory>
    private static final Map<ResourceLocation, MountCategory> moduleItems = new HashMap<>();

    //<TraitId,Provider>
    private static final Map<ResourceLocation, CapabilityEntry> traits = new HashMap<>();

    public static void registerTrait() {
        traits.putAll(
                Map.of(
                        new ResourceLocation(ZeroContact.MOD_ID, "pouch"),
                        new CapabilityEntry(CapabilityRegistries.POUCH, null, PouchProvider::new),
                        new ResourceLocation(ZeroContact.MOD_ID, "admin_pouch"),
                        new CapabilityEntry(CapabilityRegistries.ADMIN_POUCH, new AdminPouchController(), AdminPouchProvider::new),
                        new ResourceLocation(ZeroContact.MOD_ID, "navboard"),
                        new CapabilityEntry(CapabilityRegistries.NAV_BOARD, new NavBoardController(), NavBoardProvider::new),
                        new ResourceLocation(ZeroContact.MOD_ID, "nvg"),
                        new CapabilityEntry(CapabilityRegistries.NVG, new NvgController(), NvgProvider::new),
                        new ResourceLocation(ZeroContact.MOD_ID, "battery"),
                        new CapabilityEntry(CapabilityRegistries.BATTERY, null, BatteryProvider::new),
                        new ResourceLocation(ZeroContact.MOD_ID, "beacon"),
                        new CapabilityEntry(CapabilityRegistries.BEACON, new BeaconController(), BeaconProvider::new),
                        new ResourceLocation(ZeroContact.MOD_ID, "headset"),
                        new CapabilityEntry(CapabilityRegistries.HEADSET, new HeadsetController(), HeadsetProvider::new),
                        new ResourceLocation(ZeroContact.MOD_ID, "radio"),
                        new CapabilityEntry(CapabilityRegistries.RADIO, new MBITRController(), RadioProvider::new)

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
