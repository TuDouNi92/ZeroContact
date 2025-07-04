package net.zerocontact.events;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.zerocontact.api.ArmorTypeTag;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.client.renderer.AccessoriesRender;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.forge_registries.ModEntitiesReg;
import net.zerocontact.client.renderer.ArmedRaiderRender;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import net.zerocontact.registries.ItemsReg;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import static net.zerocontact.ZeroContact.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegEventBus {
    @SubscribeEvent
    public static void registerAttr(EntityAttributeCreationEvent event) {
        event.put(ModEntitiesReg.ARMED_RAIDER.get(), ArmedRaider.createAttributes().build());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntitiesReg.ARMED_RAIDER.get(), ArmedRaiderRender::new);
        RegCurioGeoItemRender();
    }

    private static void RegCurioGeoItemRender() {
        ItemsReg.ITEMS.forEach(itemRegistrySupplier -> {
            Item item = itemRegistrySupplier.get();
            if (item instanceof AbstractGenerateGeoCurioItemImpl abstractGenerateGeoCurioItem) {
                CuriosRendererRegistry.register(abstractGenerateGeoCurioItem, () -> new AccessoriesRender<>(abstractGenerateGeoCurioItem));
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterMappings(RegisterKeyMappingsEvent event) {
        KeyBindingHandler.register(event);
    }

}
