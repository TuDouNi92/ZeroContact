package net.zerocontact.forge_registries;

import com.google.gson.Gson;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.datagen.loader.ItemLoader;
import net.zerocontact.item.armband.GenerateUniformArmbandGeoImpl;
import net.zerocontact.item.armor.forge.*;
import net.zerocontact.item.forge.GeneratePlateImpl;
import net.zerocontact.item.uniform.GenerateUniformPantsGeoImpl;
import net.zerocontact.item.uniform.GenerateUniformTopGeoImpl;
import net.zerocontact.item.helmet.*;
import net.zerocontact.registries.ItemsReg;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OnRegisterItem {
    private static final Set<RegistrySupplier<? extends ItemLike>> ITEMS_TO_REG = new HashSet<>();

    @SubscribeEvent
    public static void attachToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab().equals(ItemsReg.ZERO_CONTACT.get())) {
            ITEMS_TO_REG.forEach(event::accept);
        }
    }


    @SubscribeEvent
    public static void onReg(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.ITEM)) return;
        RegistrySupplier<ForgeSpawnEggItem> RAIDER_EGG = ItemsReg.ITEMS.register("raider_egg", () -> new ForgeSpawnEggItem(ModEntitiesReg.ARMED_RAIDER, 0x3d6145, 0xcfc08a, new Item.Properties()));
        RegistrySupplier<FastMt> FAST_MT = ItemsReg.ITEMS.register("fast_mt", () -> new FastMt(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties(), 8));
        RegistrySupplier<Ratnik> RATNIK = ItemsReg.ITEMS.register("helmet_6b47_ratnik_emr", () -> new Ratnik(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties(), 6));
        RegistrySupplier<ThorArmorImpl> THOR_ARMOR = ItemsReg.ITEMS.register("armor_thor_black", () -> new ThorArmorImpl(4, 128,4,-0.01F));
        RegistrySupplier<Bastion> BASTION_HELMET = ItemsReg.ITEMS.register("helmet_bastion_black", () -> new Bastion(9, 48));
        RegistrySupplier<Untar> UNTAR_HELMET = ItemsReg.ITEMS.register("helmet_untar_blue", () -> new Untar(6, 24));
        RegistrySupplier<UntarArmorImpl> UNTAR_ARMOR = ItemsReg.ITEMS.register("armor_untar_blue", () -> new UntarArmorImpl(9, 72,6,-0.03F));
        RegistrySupplier<HexgridArmorImpl> HEXGRID_ARMOR = ItemsReg.ITEMS.register("armor_hexgrid_black", () -> new HexgridArmorImpl(9, 128,2,0.01F));
        RegistrySupplier<AltynVisor.WithVisor> ALTYN_VISOR_HELMET = ItemsReg.ITEMS.register("helmet_altyn_visor", () -> new AltynVisor.WithVisor(10, 72));
        RegistrySupplier<AirFrame> AIRFRAME_HELMET = ItemsReg.ITEMS.register("helmet_airframe", () -> new AirFrame(8, 42));
        RegistrySupplier<R6b2ArmorImpl>  R6B2 = ItemsReg.ITEMS.register("armor_6b2", () -> new R6b2ArmorImpl(5, 64,4,-0.2F));
        RegistrySupplier<R6b23IArmorImpl>  R6B23I = ItemsReg.ITEMS.register("armor_6b23_1", () -> new R6b23IArmorImpl(7, 128,7,-0.025F));
        RegistrySupplier<R6b23IIArmorImpl>  R6B23II = ItemsReg.ITEMS.register("armor_6b23_2", () -> new R6b23IIArmorImpl(7, 128,7,-0.025F));
        RegistrySupplier<Defender2ArmorImpl>  DEFENDER = ItemsReg.ITEMS.register("armor_defender_2", () -> new Defender2ArmorImpl(7, 128,4,-0.015F));


        ITEMS_TO_REG.add(RAIDER_EGG);
        ITEMS_TO_REG.add(FAST_MT);
        ITEMS_TO_REG.add(RATNIK);
        ITEMS_TO_REG.add(BASTION_HELMET);
        ITEMS_TO_REG.add(ALTYN_VISOR_HELMET);
        ITEMS_TO_REG.add(AIRFRAME_HELMET);
        ITEMS_TO_REG.add(UNTAR_HELMET);
        ITEMS_TO_REG.add(THOR_ARMOR);
        ITEMS_TO_REG.add(UNTAR_ARMOR);
        ITEMS_TO_REG.add(HEXGRID_ARMOR);
        ITEMS_TO_REG.add(R6B2);
        ITEMS_TO_REG.add(R6B23I);
        ITEMS_TO_REG.add(R6B23II);
        ITEMS_TO_REG.add(DEFENDER);

        ItemLoader.loadFromJson();
        ZeroContactLogger.LOG.info("On Register ItemData:{}", new Gson().toJson(ItemLoader.itemGenData).formatted());
        GeneratePlateImpl.deserializeItems();
        GenerateArmorGeoImpl.deserializeItems();
        GenerateHelmetGeoImpl.deserializeItems();
        GenerateUniformTopGeoImpl.deserializeItems();
        GenerateUniformPantsGeoImpl.deserializeItems();
        GenerateUniformArmbandGeoImpl.deserializeItems();
        GenerateCarrierGeoImpl.deserializeItems();
        registerGeneratedItems(GeneratePlateImpl.items, "PLATE");
        registerGeneratedItems(GenerateArmorGeoImpl.items, "ARMOR");
        registerGeneratedItems(GenerateCarrierGeoImpl.items,"PLATE_CARRIER");
        registerGeneratedItems(GenerateHelmetGeoImpl.items, "HELMET");
        registerGeneratedItems(GenerateUniformTopGeoImpl.items, "UNIFORM_TOP");
        registerGeneratedItems(GenerateUniformPantsGeoImpl.items, "UNIFORM_PANTS");
        registerGeneratedItems(GenerateUniformArmbandGeoImpl.items, "ARMBAND");
    }

    private static void registerGeneratedItems(Set<? extends GenerationRecord> records, String type) {
        records.forEach(record -> {
            RegistrySupplier<Item> reg = ItemsReg.ITEMS.register(record.id(), record::item);
            ITEMS_TO_REG.add(reg);
            ZeroContactLogger.LOG.info("Reg {} for:{}", type, reg);
        });
    }
}
