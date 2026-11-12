package net.zerocontact.armor.modular.capability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.EquipmentModule;
import net.zerocontact.api.armor.modular.ModularCapabilityAccess;
import net.zerocontact.api.armor.modular.ModularEquipment;
import net.zerocontact.api.armor.modular.ModuleContainer;
import net.zerocontact.armor.modular.container.SimpleModuleContainer;
import net.zerocontact.armor.modular.module.battery.container.BatteryContainer;
import net.zerocontact.armor.modular.module.beacon.container.BeaconContainer;
import net.zerocontact.armor.modular.module.headset.container.HeadsetContainer;
import net.zerocontact.armor.modular.module.nvg.container.NvgContainer;
import net.zerocontact.armor.modular.module.pouch.container.AdminPouchContainer;
import net.zerocontact.armor.modular.module.pouch.container.PouchContainer;
import net.zerocontact.armor.modular.module.pouch.container.navboard.NavBoardContainer;
import net.zerocontact.armor.modular.module.radio.api.MBITR;
import net.zerocontact.armor.modular.registry.ModuleRegistry;

import java.util.function.Function;

public final class ModularCapabilityRegistry {
    public static final ItemCapability<ModuleContainer, Void> MODULAR_EQUIPMENT = create("modular_equipment", ModuleContainer.class);
    public static final ItemCapability<AdminPouchContainer, Void> ADMIN_POUCH = create("admin_pouch", AdminPouchContainer.class);
    public static final ItemCapability<NavBoardContainer, Void> NAV_BOARD = create("nav_board", NavBoardContainer.class);
    public static final ItemCapability<PouchContainer, Void> POUCH = create("pouch", PouchContainer.class);
    public static final ItemCapability<NvgContainer, Void> NVG = create("nvg", NvgContainer.class);
    public static final ItemCapability<BatteryContainer, Void> BATTERY = create("battery", BatteryContainer.class);
    public static final ItemCapability<BeaconContainer, Void> BEACON = create("beacon", BeaconContainer.class);
    public static final ItemCapability<HeadsetContainer, Void> HEADSET = create("headset", HeadsetContainer.class);
    public static final ItemCapability<MBITR, Void> RADIO = create("radio", MBITR.class);

    private ModularCapabilityRegistry() {}

    private static <T> ItemCapability<T, Void> create(String id, Class<T> type) {
        return ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, id), type);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ModularEquipment) {
                register(event, item, MODULAR_EQUIPMENT, SimpleModuleContainer::new);
            }
            if (item instanceof EquipmentModule module) {
                var trait = ModuleRegistry.getTrait(module.getModuleTrait());
                if (trait != null) registerTrait(event, item, trait);
            }
        }
    }

    private static <T> void registerTrait(RegisterCapabilitiesEvent event, Item item, ModuleRegistry.CapabilityEntry<T> trait) {
        register(event, item, trait.capability(), trait.containerFactory());
    }

    private static <T> void register(RegisterCapabilitiesEvent event, Item item, ItemCapability<T, Void> capability,
                                     Function<ItemStack, T> factory) {
        event.registerItem(capability, (stack, context) ->
                ((ModularCapabilityAccess) (Object) stack).zeroContact$getModularCapability(capability, factory), item);
    }
}
