package net.zerocontact.forge_registries;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.item.forge.GenerateImpl;
import net.zerocontact.registries.ItemsReg;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OnRegisterItem {
    private static RegistrySupplier<ForgeSpawnEggItem> RAIDER_EGG;
    @SubscribeEvent
    public static void attachToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab().equals(ItemsReg.ZERO_CONTACT.get())) {
            if (GenerateImpl.items.isEmpty()) return;
            GenerateImpl.items.forEach(event::accept);
            if(RAIDER_EGG != null){
                event.accept(RAIDER_EGG);
            }
        }
    }

    @SubscribeEvent
    public static void onReg(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.ITEM)) return;
        RAIDER_EGG =ItemsReg.ITEMS.register("raider_egg", () -> new ForgeSpawnEggItem(ModEntitiesReg.ARMED_RAIDER, 0x3d6145, 0xcfc08a, new Item.Properties()));
        GenerateImpl.regItems();
        if (GenerateImpl.items.isEmpty()) return;
        GenerateImpl.items.forEach(item -> ItemsReg.ITEMS.register(item.id, () -> item));

    }
}
