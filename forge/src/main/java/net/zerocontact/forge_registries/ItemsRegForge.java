package net.zerocontact.forge_registries;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.IAssetManager;
import net.zerocontact.api.TabableItem;
import net.zerocontact.datagen.loader.ZContentLoader;
import net.zerocontact.datagen.loader.ZPackManager;
import net.zerocontact.forge_registries.item.*;
import net.zerocontact.item.armband.GenerateUniformArmbandGeoImpl;
import net.zerocontact.item.armor.forge.*;
import net.zerocontact.item.dogtag.DogTag;
import net.zerocontact.item.forge.GeneratePlateImpl;
import net.zerocontact.item.uniform.*;
import net.zerocontact.item.helmet.*;
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

        RegistrySupplier<GeneratePlateImpl> GENERATE_PLATE = ItemsReg.ITEMS.register("generate_plate", () -> new GeneratePlateImpl("", 0, 0, 0, 0, 0, 0, 0, 0));
        RegistrySupplier<GenerateArmorGeoImpl> GENERATE_ARMOR = ItemsReg.ITEMS.register("generate_armor", () -> new GenerateArmorGeoImpl(ArmorItem.Type.CHESTPLATE, "", 0, 0, 0, 0, new ResourceLocation(""), new ResourceLocation(""), new ResourceLocation(""), 0, 0, 0));
        RegistrySupplier<GenerateCarrierGeoImpl> GENERATE_CARRIER = ItemsReg.ITEMS.register("generate_carrier", () -> new GenerateCarrierGeoImpl(ArmorItem.Type.CHESTPLATE, "", 0, 0, 0, 0, 0, 0, new ResourceLocation(""), new ResourceLocation(""), new ResourceLocation("")));
        RegistrySupplier<GenerateHelmetGeoImpl> GENERATE_HELMET = ItemsReg.ITEMS.register("generate_helmet", () -> new GenerateHelmetGeoImpl("", ArmorItem.Type.HELMET, new ResourceLocation(""), new ResourceLocation(""), new ResourceLocation(""), 0, 0, 0, 0, 0, 0, 0));
        RegistrySupplier<GenerateUniformTopGeoImpl> GENERATE_TOP_UNIFORM = ItemsReg.ITEMS.register("generate_top_uniform", () -> new GenerateUniformTopGeoImpl("", 0, new ResourceLocation(""), new ResourceLocation(""), new ResourceLocation("")));
        RegistrySupplier<GenerateUniformPantsGeoImpl> GENERATE_PANTS = ItemsReg.ITEMS.register("generate_pants", () -> new GenerateUniformPantsGeoImpl("", 0, new ResourceLocation(""), new ResourceLocation(""), new ResourceLocation("")));
        RegistrySupplier<GenerateUniformArmbandGeoImpl> GENERATE_ARMBAND = ItemsReg.ITEMS.register("generate_armband", () -> new GenerateUniformArmbandGeoImpl("", 0, new ResourceLocation(""), new ResourceLocation(""), new ResourceLocation("")));

        RegistrySupplier<ForgeSpawnEggItem> RAIDER_EGG = ItemsReg.ITEMS.register("raider_egg", () -> new ForgeSpawnEggItem(ModEntitiesReg.ARMED_RAIDER, 0x3d6145, 0xcfc08a, new Item.Properties()));
        List<TabableItem> onGoingRegItems = List.of(
                new Plates(),
                new Helmets(),
                new Armors(),
                new Backpacks(),
                new Armbands(),
                new Uniforms()
        );
        onGoingRegItems.forEach(reg -> reg.attach(ITEMS_REG_TAB));
        List<RegistrySupplier<? extends ItemLike>> items = List.of(
                RAIDER_EGG);
        items.forEach(item -> ITEMS_REG_TAB.put(item, DEFAULT_TAB));
        ZPackManager packManager = new ZPackManager();
        packManager.init();
        IAssetManager assetManager = packManager.getAssetManager();
        ZeroContactLogger.LOG.info("On Register ItemData:{}", assetManager.getGson().toJson(ZContentLoader.itemGenData).formatted());
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
