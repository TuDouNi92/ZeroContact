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
import net.zerocontact.api.IAssetManager;
import net.zerocontact.api.TabableItem;
import net.zerocontact.datagen.loader.ZPackManager;
import net.zerocontact.forge_registries.item.*;
import net.zerocontact.item.kit.ArmorRepairKit;
import net.zerocontact.item.dogtag.DogTag;
import net.zerocontact.registries.ItemsReg;

import java.util.*;

import static net.zerocontact.ZeroContact.MOD_ID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemsRegForge {
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
        RegistrySupplier<ForgeSpawnEggItem> RAIDER_EGG = ItemsReg.ITEMS.register("raider_egg", () -> new ForgeSpawnEggItem(ModEntitiesReg.ARMED_RAIDER, 0x3d6145, 0xcfc08a, new Item.Properties()));
        RegistrySupplier<ArmorRepairKit> ARMOR_KIT = ItemsReg.ITEMS.register("kit_armor", ArmorRepairKit::new);
        List<TabableItem> onGoingRegItems = List.of(
                new Plates(),
                new Helmets(),
                new Armors(),
                new Loadouts(),
                new Armbands(),
                new Uniforms()
        );
        onGoingRegItems.forEach(reg -> reg.attach(ITEMS_REG_TAB));
        List<RegistrySupplier<? extends ItemLike>> items = List.of(ARMOR_KIT,RAIDER_EGG);
        items.forEach(item -> ITEMS_REG_TAB.put(item, DEFAULT_TAB));
        ZPackManager packManager = new ZPackManager();
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

        ModMenus.MENUS.register();
    }

}
