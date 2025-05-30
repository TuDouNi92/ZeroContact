package net.zerocontact.forge_registries;

import net.minecraft.core.registries.Registries;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.zerocontact.item.forge.GenerateImpl;
import net.zerocontact.registries.ItemsReg;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class OnRegisterItem {
    @SubscribeEvent
    public static void attachToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab().equals(ItemsReg.ZERO_CONTACT.get())) {
            if (GenerateImpl.items.isEmpty()) return;
            GenerateImpl.items.forEach(event::accept);
        }
    }

    @SubscribeEvent
    public static void onReg(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.ITEM)) return;
        GenerateImpl.regItems();
        if (GenerateImpl.items.isEmpty()) return;
        GenerateImpl.items.forEach(item -> ItemsReg.ITEMS.register(item.id, () -> item));

    }
}
