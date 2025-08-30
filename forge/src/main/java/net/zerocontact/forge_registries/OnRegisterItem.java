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
import net.zerocontact.item.armband.Armband;
import net.zerocontact.item.armband.GenerateUniformArmbandGeoImpl;
import net.zerocontact.item.armor.forge.*;
import net.zerocontact.item.backpack.T20;
import net.zerocontact.item.dogtag.DogTag;
import net.zerocontact.item.forge.GeneratePlateImpl;
import net.zerocontact.item.uniform.GenerateUniformPantsGeoImpl;
import net.zerocontact.item.uniform.GenerateUniformTopGeoImpl;
import net.zerocontact.item.helmet.*;
import net.zerocontact.registries.ItemsReg;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OnRegisterItem {
    private static final LinkedHashSet<RegistrySupplier<? extends ItemLike>> ITEMS_TO_REG = new LinkedHashSet<>();

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
        RegistrySupplier<Ratnik> RATNIK_HELMET_EMR = ItemsReg.ITEMS.register("helmet_6b47_ratnik_emr", () -> Ratnik.create(7,32, Ratnik.Color.EMR));
        RegistrySupplier<Ratnik> RATNIK_HELMET_ARC = ItemsReg.ITEMS.register("helmet_6b47_ratnik_arc", () -> Ratnik.create(7,32, Ratnik.Color.ARCTIC));
        RegistrySupplier<ThorArmorImpl> THOR_ARMOR = ItemsReg.ITEMS.register("armor_thor_black", () -> new ThorArmorImpl(4, 128,4,-0.01F));
        RegistrySupplier<Bastion> BASTION_HELMET = ItemsReg.ITEMS.register("helmet_bastion_black", () -> Bastion.create(9,48, Bastion.Color.BLACK));
        RegistrySupplier<Bastion> BASTION_HELMET_MULTICAM = ItemsReg.ITEMS.register("helmet_bastion_multicam", () -> Bastion.create(9,48, Bastion.Color.MULTICAM));
        RegistrySupplier<Bastion> BASTION_HELMET_GREEN = ItemsReg.ITEMS.register("helmet_bastion_green", () -> Bastion.create(9,48, Bastion.Color.GREEN));
        RegistrySupplier<Untar> UNTAR_HELMET = ItemsReg.ITEMS.register("helmet_untar_blue", () -> new Untar(6, 24));
        RegistrySupplier<TagillaMask> TAGILLA_MASK_MANHUNT = ItemsReg.ITEMS.register("mask_tagilla_manhunt",()->TagillaMask.create(12,64, TagillaMask.Color.MANHUNT));
        RegistrySupplier<TagillaMask> TAGILLA_MASK_YBEY = ItemsReg.ITEMS.register("mask_tagilla_ybey",()->TagillaMask.create(12,64, TagillaMask.Color.YBEY));
        RegistrySupplier<ColdFearMask> COLD_FEAR_MASK = ItemsReg.ITEMS.register("mask_cold_fear",()->new ColdFearMask(8,24));
        RegistrySupplier<CyanCap> CYAN_CAP = ItemsReg.ITEMS.register("cap_cyan",()->new CyanCap(2,24));
        RegistrySupplier<UntarArmorImpl> UNTAR_ARMOR = ItemsReg.ITEMS.register("armor_untar_blue", () -> new UntarArmorImpl(9, 72,6,-0.03F));
        RegistrySupplier<HexgridArmorImpl> HEXGRID_ARMOR = ItemsReg.ITEMS.register("armor_hexgrid_black", () -> new HexgridArmorImpl(9, 128,2,0.01F));
        RegistrySupplier<AltynVisor.WithVisor> ALTYN_VISOR_HELMET = ItemsReg.ITEMS.register("helmet_altyn_visor", () -> new AltynVisor.WithVisor(10, 72));
        RegistrySupplier<AirFrame> AIRFRAME_HELMET = ItemsReg.ITEMS.register("helmet_airframe", () -> new AirFrame(8, 42));
        RegistrySupplier<R6b2ArmorImpl>  R6B2 = ItemsReg.ITEMS.register("armor_6b2", () -> new R6b2ArmorImpl(5, 64,4,-0.2F));
        RegistrySupplier<R6b23IArmorImpl>  R6B23I = ItemsReg.ITEMS.register("armor_6b23_1", () -> new R6b23IArmorImpl(7, 128,7,-0.025F));
        RegistrySupplier<R6b23IIArmorImpl>  R6B23II = ItemsReg.ITEMS.register("armor_6b23_2", () -> new R6b23IIArmorImpl(7, 128,7,-0.025F));
        RegistrySupplier<Defender2ArmorImpl>  DEFENDER = ItemsReg.ITEMS.register("armor_defender_2", () -> new Defender2ArmorImpl(7, 128,4,-0.015F));
        RegistrySupplier<T20> T20_BACKPACK_UMBRA = ItemsReg.ITEMS.register("backpack_t20_umbra",()->T20.create(T20.Series.UMBRA,25));
        RegistrySupplier<T20> T20_BACKPACK_MULTICAM = ItemsReg.ITEMS.register("backpack_t20_multicam",()->T20.create(T20.Series.MULTICAM,25));
        RegistrySupplier<DogTag> DOG_TAG = ItemsReg.ITEMS.register("dog_tag",()->new DogTag(new Item.Properties()));
        RegistrySupplier<Armband> ARMBAND_BLACK = ItemsReg.ITEMS.register("armband_black",()->Armband.create(Armband.Series.BLACK));
        RegistrySupplier<Armband> ARMBAND_RED = ItemsReg.ITEMS.register("armband_red",()->Armband.create(Armband.Series.RED));
        RegistrySupplier<Armband> ARMBAND_GREEN = ItemsReg.ITEMS.register("armband_green",()->Armband.create(Armband.Series.GREEN));
        RegistrySupplier<Armband> ARMBAND_BLUE = ItemsReg.ITEMS.register("armband_blue",()->Armband.create(Armband.Series.BLUE));
        RegistrySupplier<Armband> ARMBAND_WHITE = ItemsReg.ITEMS.register("armband_white",()->Armband.create(Armband.Series.WHITE));
        RegistrySupplier<Armband> ARMBAND_YELLOW = ItemsReg.ITEMS.register("armband_yellow",()->Armband.create(Armband.Series.YELLOW));
        RegistrySupplier<Armband> ARMBAND_FLORA = ItemsReg.ITEMS.register("armband_flora",()->Armband.create(Armband.Series.FLORA));

        ITEMS_TO_REG.addAll(
                List.of(
                        RAIDER_EGG,
                        RATNIK_HELMET_EMR,RATNIK_HELMET_ARC,BASTION_HELMET,BASTION_HELMET_MULTICAM,BASTION_HELMET_GREEN,
                        ALTYN_VISOR_HELMET,AIRFRAME_HELMET,UNTAR_HELMET,TAGILLA_MASK_MANHUNT,TAGILLA_MASK_YBEY,COLD_FEAR_MASK,CYAN_CAP,
                        THOR_ARMOR,UNTAR_ARMOR,HEXGRID_ARMOR,R6B2,R6B23I,R6B23II,DEFENDER,
                        T20_BACKPACK_MULTICAM,T20_BACKPACK_UMBRA,
                        ARMBAND_BLACK,ARMBAND_RED,ARMBAND_BLUE,ARMBAND_WHITE,ARMBAND_GREEN,ARMBAND_YELLOW,ARMBAND_FLORA
                )
        );
        ItemLoader.loadFromJson();
        ZeroContactLogger.LOG.info("On Register ItemData:{}", new Gson().toJson(ItemLoader.itemGenData).formatted());
        GeneratePlateImpl.deserializeItems();
        GenerateArmorGeoImpl.deserializeItems();
        GenerateHelmetGeoImpl.deserializeItems();
        GenerateUniformTopGeoImpl.deserializeItems();
        GenerateUniformPantsGeoImpl.deserializeItems();
        GenerateUniformArmbandGeoImpl.deserializeItems();
        GenerateCarrierGeoImpl.deserializeItems();
        registerGeneratedItems(
                new WearableType(GeneratePlateImpl.items, "PLATE"),
                new WearableType(GenerateArmorGeoImpl.items, "ARMOR"),
                new WearableType(GenerateCarrierGeoImpl.items,"PLATE_CARRIER"),
                new WearableType(GenerateHelmetGeoImpl.items, "HELMET"),
                new WearableType(GenerateUniformTopGeoImpl.items, "UNIFORM_TOP"),
                new WearableType(GenerateUniformPantsGeoImpl.items, "UNIFORM_PANTS"),
                new WearableType(GenerateUniformArmbandGeoImpl.items, "ARMBAND")
        );
        ModMenus.MENUS.register();
    }
    private record WearableType(Set<GenerationRecord> records, String logDisplayName){}
    private static void registerGeneratedItems(WearableType... wearableTypes) {
        for(WearableType type:wearableTypes){
            type.records.forEach(generationRecord -> {
                RegistrySupplier<Item> reg = ItemsReg.ITEMS.register(generationRecord.id(), generationRecord::item);
                ITEMS_TO_REG.add(reg);
                ZeroContactLogger.LOG.info("Reg {} for:{}", type.logDisplayName, reg);
            });
        }
    }
}
