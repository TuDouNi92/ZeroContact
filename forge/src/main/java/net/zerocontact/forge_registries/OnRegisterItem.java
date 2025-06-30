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
import net.zerocontact.datagen.loader.ItemLoader;
import net.zerocontact.item.armor.forge.GenerateArmorGeoImpl;
import net.zerocontact.item.armor.forge.ThorArmorImpl;
import net.zerocontact.item.forge.GeneratePlateImpl;
import net.zerocontact.item.forge.GenerateUniformGeoImpl;
import net.zerocontact.item.helmet.Bastion;
import net.zerocontact.item.helmet.FastMt;
import net.zerocontact.item.helmet.GenerateHelmetGeoImpl;
import net.zerocontact.item.helmet.Ratnik;
import net.zerocontact.registries.ItemsReg;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OnRegisterItem {
    private static final Set<RegistrySupplier<? extends ItemLike>> ITEMS_TO_REG = new HashSet<>();

    @SubscribeEvent
    public static void attachToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab().equals(ItemsReg.ZERO_CONTACT.get())) {
            attachGenerated(
                    event,
                    GeneratePlateImpl.items,
                    GenerateArmorGeoImpl.items,
                    GenerateHelmetGeoImpl.items
                    );
            ITEMS_TO_REG.forEach(event::accept);
        }
    }

    @SafeVarargs
    private static void attachGenerated(BuildCreativeModeTabContentsEvent event, Set<? extends ItemLike>... itemLists) {
        for (Set<? extends ItemLike> itemList : itemLists) {
            if (itemList.isEmpty()) return;
            itemList.forEach(event::accept);
        }
    }

    @SubscribeEvent
    public static void onReg(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.ITEM)) return;
        RegistrySupplier<ForgeSpawnEggItem> RAIDER_EGG = ItemsReg.ITEMS.register("raider_egg", () -> new ForgeSpawnEggItem(ModEntitiesReg.ARMED_RAIDER, 0x3d6145, 0xcfc08a, new Item.Properties()));
        RegistrySupplier<FastMt> FAST_MT = ItemsReg.ITEMS.register("fast_mt", () -> new FastMt(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties(), 8));
        RegistrySupplier<Ratnik> RATNIK = ItemsReg.ITEMS.register("6b47_ratnik", () -> new Ratnik(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties(), 6));
        RegistrySupplier<ThorArmorImpl> THOR_ARMOR = ItemsReg.ITEMS.register("armor_thor_black", () -> new ThorArmorImpl(4,128));
        RegistrySupplier<Bastion> BASTION_HELMET = ItemsReg.ITEMS.register("helmet_bastion_black", () -> new Bastion(9,48));
        ITEMS_TO_REG.add(RAIDER_EGG);
        ITEMS_TO_REG.add(FAST_MT);
        ITEMS_TO_REG.add(RATNIK);
        ITEMS_TO_REG.add(THOR_ARMOR);
        ITEMS_TO_REG.add(BASTION_HELMET);
        ItemLoader.loadFromJson();
        ZeroContactLogger.LOG.info("On Register ItemData:{}", new Gson().toJson(ItemLoader.itemGenData).formatted());
        GeneratePlateImpl.regItems();
        GenerateArmorGeoImpl.regItems();
        GenerateHelmetGeoImpl.regItems();
        GenerateUniformGeoImpl.regItems();
        GeneratePlateImpl.items.forEach(item -> {
            ItemsReg.ITEMS.register(item.id, () -> item);
            ZeroContactLogger.LOG.info("Reg plate for:{}", item);
        });
        GenerateArmorGeoImpl.items.forEach(item -> {
            ItemsReg.ITEMS.register(item.id, () -> item);
            ZeroContactLogger.LOG.info("Reg armor for:{}", item);
        });
        GenerateHelmetGeoImpl.items.forEach(item -> {
            ItemsReg.ITEMS.register(item.id, () -> item);
            ZeroContactLogger.LOG.info("Reg helmet for:{}", item);
        });
        GenerateUniformGeoImpl.items.forEach(item -> {
            ItemsReg.ITEMS.register(item.id, () -> item);
            ZeroContactLogger.LOG.info("Reg uniform for:{}", item);
        });
    }
}
