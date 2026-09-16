package net.zerocontact.forge_registries;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.zerocontact.api.datagen.IAssetManager;
import net.zerocontact.api.TabableItem;
import net.zerocontact.armor.modular.module.battery.item.Battery;
import net.zerocontact.armor.modular.module.beacon.item.Beacon;
import net.zerocontact.armor.modular.module.headset.item.ComtacII;
import net.zerocontact.armor.modular.module.headset.item.Headset;
import net.zerocontact.armor.modular.module.nvg.item.GPNVG;
import net.zerocontact.armor.modular.module.nvg.item.PVS31;
import net.zerocontact.armor.modular.module.nvg.item.PVS31A;
import net.zerocontact.armor.modular.module.nvg.item.T7;
import net.zerocontact.datagen.loader.ZPackManager;
import net.zerocontact.forge_registries.item.*;
import net.zerocontact.item.kit.ArmorRepairKit;
import net.zerocontact.item.dogtag.DogTag;
import net.zerocontact.registries.ItemsReg;

import java.util.*;

import static net.zerocontact.ZeroContact.MOD_ID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemRegistry {
    public static final LinkedHashMap<RegistrySupplier<? extends ItemLike>, String> ITEMS_REG_TAB = new LinkedHashMap<>();
    private static final LinkedHashMap<RegistrySupplier<CreativeModeTab>, String> TABS = new LinkedHashMap<>();
    public static final String DEFAULT_TAB = "zero_contact";

    @SubscribeEvent
    public static void attachToTabs(BuildCreativeModeTabContentsEvent event) {
        TABS.forEach((tab, name) -> {
            if (!event.getTab().equals(tab.get())) return;
            ITEMS_REG_TAB.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(name))
                    .forEach(entry -> event.accept(entry.getKey()));
        });
    }


    @SubscribeEvent
    public static void onReg(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.ITEM)) return;
        RegistrySupplier<DogTag> DOG_TAG = ItemsReg.ITEMS.register("dog_tag", () -> new DogTag(new Item.Properties()));
        RegistrySupplier<ForgeSpawnEggItem> RAIDER_EGG = ItemsReg.ITEMS.register("raider_egg", () -> new ForgeSpawnEggItem(EntitiyRegistry.ARMED_RAIDER, 0x3d6145, 0xcfc08a, new Item.Properties()));
        RegistrySupplier<ArmorRepairKit> ARMOR_KIT = ItemsReg.ITEMS.register("kit_armor", ArmorRepairKit::new);
        RegistrySupplier<PVS31> NVG_PVS31 = ItemsReg.ITEMS.register("nvg_pvs31", PVS31::new);
        RegistrySupplier<PVS31A> NVG_PVS31A = ItemsReg.ITEMS.register("nvg_pvs31a", PVS31A::new);
        RegistrySupplier<Battery> BATTERY = ItemsReg.ITEMS.register("battery", Battery::new);
        RegistrySupplier<Beacon> BEACON = ItemsReg.ITEMS.register("beacon", Beacon::new);
        RegistrySupplier<T7> NVG_T7 = ItemsReg.ITEMS.register("nvg_t7", T7::new);
        RegistrySupplier<GPNVG> NVG_GPNVG = ItemsReg.ITEMS.register("nvg_gpnvg", GPNVG::new);
        RegistrySupplier<Headset> COMTAC_II = ItemsReg.ITEMS.register("headset_comtac2", ComtacII::new);
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
                NVG_PVS31,
                NVG_PVS31A,
                NVG_T7,
                NVG_GPNVG,
                BATTERY,
                BEACON,
                COMTAC_II,
                RAIDER_EGG
        );
        items.forEach(item -> ITEMS_REG_TAB.put(item, DEFAULT_TAB));
        ZPackManager packManager = ZPackManager.getInstance();
        packManager.init();
        IAssetManager assetManager = packManager.getAssetManager();
        assetManager.register();

        LinkedHashSet<String> tabNameSet = new LinkedHashSet<>(ITEMS_REG_TAB.values());
        tabNameSet.forEach(tabName -> {
            ItemStack[] iconStack = {ItemStack.EMPTY};
            ITEMS_REG_TAB.entrySet().stream()
                    .filter(entry -> Objects.equals(entry.getValue(), tabName))
                    .findFirst()
                    .ifPresent(entry -> iconStack[0] = new ItemStack(entry.getKey().get()));
            TABS.put(ItemsReg.TABS.register(tabName, () -> CreativeTabRegistry.create(Component.translatable("itemGroup." + MOD_ID + "." + tabName), () -> iconStack[0])), tabName);
        });

        MenuRegistry.MENUS.register();
    }

}
