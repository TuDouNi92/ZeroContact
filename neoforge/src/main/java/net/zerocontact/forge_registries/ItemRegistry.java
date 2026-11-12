package net.zerocontact.forge_registries;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.datagen.IAssetManager;
import net.zerocontact.api.TabableItem;
import net.zerocontact.armor.modular.module.battery.item.Battery;
import net.zerocontact.armor.modular.module.beacon.item.Beacon;
import net.zerocontact.armor.modular.module.headset.item.Amp;
import net.zerocontact.armor.modular.module.headset.item.ComtacII;
import net.zerocontact.armor.modular.module.headset.item.Headset;
import net.zerocontact.armor.modular.module.nvg.item.GPNVG;
import net.zerocontact.armor.modular.module.nvg.item.PVS31;
import net.zerocontact.armor.modular.module.nvg.item.PVS31A;
import net.zerocontact.armor.modular.module.nvg.item.T7;
import net.zerocontact.datagen.loader.ZPackManager;
import net.zerocontact.forge_registries.item.*;
import net.zerocontact.item.kit.ArmorRepairKit;
import net.zerocontact.registries.ItemsReg;

import java.util.*;

import static net.zerocontact.ZeroContact.MOD_ID;

@EventBusSubscriber(modid = ZeroContact.MOD_ID)
public class ItemRegistry {
    public static final LinkedHashMap<RegistrySupplier<? extends ItemLike>, String> ITEMS_REG_TAB = new LinkedHashMap<>();
    private static final LinkedHashMap<ResourceKey<CreativeModeTab>, String> TABS = new LinkedHashMap<>();
    public static final String DEFAULT_TAB = "zero_contact";
    public static final String MODULE_TAB = "zero_contact_module";
    public static final ResourceKey<CreativeModeTab> DEFAULT_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(MOD_ID, DEFAULT_TAB));


    @SubscribeEvent
    public static void attachToTabs(BuildCreativeModeTabContentsEvent event) {
        TABS.forEach((tab, name) -> {
            if (!event.getTabKey().equals(tab)) return;
            ITEMS_REG_TAB.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(name))
                    .forEach(entry -> event.accept(entry.getKey().get()));
        });
    }


    // Populate the deferred entries before Architectury's NORMAL-priority registrar runs.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onReg(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
            registerTabs(event);
            return;
        }
        if (!event.getRegistryKey().equals(Registries.ITEM)) return;
        RegistrySupplier<DeferredSpawnEggItem> RAIDER_EGG = ItemsReg.ITEMS.register("raider_egg", () -> new DeferredSpawnEggItem(EntitiyRegistry.ARMED_RAIDER, 0x3d6145, 0xcfc08a, new Item.Properties()));
        RegistrySupplier<ArmorRepairKit> ARMOR_KIT = ItemsReg.ITEMS.register("kit_armor", ArmorRepairKit::new);
        RegistrySupplier<PVS31> NVG_PVS31 = ItemsReg.ITEMS.register("nvg_pvs31", PVS31::new);
        RegistrySupplier<PVS31A> NVG_PVS31A = ItemsReg.ITEMS.register("nvg_pvs31a", PVS31A::new);
        RegistrySupplier<Battery> BATTERY = ItemsReg.ITEMS.register("battery", Battery::new);
        RegistrySupplier<Beacon> BEACON = ItemsReg.ITEMS.register("beacon", Beacon::new);
        RegistrySupplier<T7> NVG_T7 = ItemsReg.ITEMS.register("nvg_t7", T7::new);
        RegistrySupplier<GPNVG> NVG_GPNVG = ItemsReg.ITEMS.register("nvg_gpnvg", GPNVG::new);
        RegistrySupplier<Headset> COMTAC_II = ItemsReg.ITEMS.register("headset_comtac2", ()->new ComtacII("headset_comtac2",ComtacII.Color.OD));
        RegistrySupplier<Headset> COMTAC_II_BROWN = ItemsReg.ITEMS.register("headset_comtac2_brown", ()->new ComtacII("headset_comtac2_brown",ComtacII.Color.CB));
        RegistrySupplier<Headset> AMP = ItemsReg.ITEMS.register("headset_amp", ()->new Amp("headset_amp", Amp.Color.TAN));
        RegistrySupplier<Headset> AMP_BLACK = ItemsReg.ITEMS.register("headset_amp_black", ()->new Amp("headset_amp_black", Amp.Color.BLACK));

        List<TabableItem> onGoingRegItems = List.of(
                new Plates(),
                new Helmets(),
                new Armors(),
                new Loadouts(),
                new Armbands(),
                new Uniforms()
        );
        onGoingRegItems.forEach(reg -> reg.attach(ITEMS_REG_TAB));
        List<RegistrySupplier<? extends ItemLike>> items = List.of(
                ARMOR_KIT,
                RAIDER_EGG
        );
        List<RegistrySupplier<? extends ItemLike>> moduleItems = List.of(
                NVG_PVS31,
                NVG_PVS31A,
                NVG_T7,
                NVG_GPNVG,
                BATTERY,
                BEACON,
                AMP,
                AMP_BLACK,
                COMTAC_II,
                COMTAC_II_BROWN
        );
        items.forEach(item -> ITEMS_REG_TAB.put(item, DEFAULT_TAB));
        moduleItems.forEach(item -> ITEMS_REG_TAB.put(item, MODULE_TAB));
        ZPackManager packManager = ZPackManager.getInstance();
        packManager.init();
        IAssetManager assetManager = packManager.getAssetManager();
        assetManager.register();

        MenuRegistry.MENUS.register();
    }

    private static void registerTabs(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, helper -> {
            LinkedHashSet<String> tabNames = new LinkedHashSet<>(ITEMS_REG_TAB.values());
            tabNames.forEach(tabName -> {
                ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(
                        Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(MOD_ID, tabName));
                RegistrySupplier<? extends ItemLike> icon = ITEMS_REG_TAB.entrySet().stream()
                        .filter(entry -> Objects.equals(entry.getValue(), tabName))
                        .findFirst().orElseThrow().getKey();
                helper.register(tabKey, CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup." + MOD_ID + "." + tabName))
                        .icon(() -> new ItemStack(icon.get()))
                        .build());
                TABS.put(tabKey, tabName);
            });
        });
    }

}
