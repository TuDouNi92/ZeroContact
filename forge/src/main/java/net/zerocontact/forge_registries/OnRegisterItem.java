package net.zerocontact.forge_registries;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.item.forge.GenerateImpl;
import net.zerocontact.item.forge.Helmet;
import net.zerocontact.registries.ItemsReg;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OnRegisterItem {
    private static final Set<RegistrySupplier<? extends ItemLike>> ITEMS_TO_REG=new HashSet<>();
    @SubscribeEvent
    public static void attachToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab().equals(ItemsReg.ZERO_CONTACT.get())) {
            if (GenerateImpl.items.isEmpty()) return;
            GenerateImpl.items.forEach(event::accept);
            ITEMS_TO_REG.forEach(event::accept);
        }
    }

    @SubscribeEvent
    public static void onReg(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.ITEM)) return;
        RegistrySupplier<ForgeSpawnEggItem> RAIDER_EGG =ItemsReg.ITEMS.register("raider_egg", () -> new ForgeSpawnEggItem(ModEntitiesReg.ARMED_RAIDER, 0x3d6145, 0xcfc08a, new Item.Properties()));
        RegistrySupplier<Helmet> FAST_MT = ItemsReg.ITEMS.register("fast_mt",()->new Helmet(ArmorMaterials.IRON, ArmorItem.Type.HELMET,new Item.Properties()));
        ITEMS_TO_REG.add(RAIDER_EGG);
        ITEMS_TO_REG.add(FAST_MT);
        GenerateImpl.regItems();
        if (GenerateImpl.items.isEmpty()) return;
        GenerateImpl.items.forEach(item -> ItemsReg.ITEMS.register(item.id, () -> item));

    }
}
