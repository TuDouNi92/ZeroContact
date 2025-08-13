package net.zerocontact.events;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.zerocontact.client.gui.BackpackScreen;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.forge_registries.ModMenus;
import net.zerocontact.client.renderer.AccessoriesRender;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.forge_registries.ModEntitiesReg;
import net.zerocontact.client.renderer.ArmedRaiderRender;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import net.zerocontact.registries.ItemsReg;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;


public class ModRegEventBus {
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    static class ServerDistribution{
        @SubscribeEvent
        public static void registerAttr(EntityAttributeCreationEvent event) {
            event.put(ModEntitiesReg.ARMED_RAIDER.get(), ArmedRaider.createAttributes().build());
        }
    }
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
    static class ClientDistribution {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntitiesReg.ARMED_RAIDER.get(), ArmedRaiderRender::new);
            MenuScreens.register(ModMenus.BACKPACK_CONTAINER.get(), BackpackScreen::new);
            RegCurioGeoItemRender();
        }
        @SubscribeEvent
        public static void onRegisterMappings(RegisterKeyMappingsEvent event) {
            KeyBindingHandler.register(event);
        }
    }

    private static void RegCurioGeoItemRender() {
        ItemsReg.ITEMS.forEach(itemRegistrySupplier -> {
            Item item = itemRegistrySupplier.get();
            if (item instanceof AbstractGenerateGeoCurioItemImpl abstractGenerateGeoCurioItem) {
                CuriosRendererRegistry.register(abstractGenerateGeoCurioItem, () -> new AccessoriesRender<>(abstractGenerateGeoCurioItem));
            }
        });
    }
}
