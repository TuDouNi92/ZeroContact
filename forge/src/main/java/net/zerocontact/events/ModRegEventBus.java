package net.zerocontact.events;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.forge_registries.ModEntitiesReg;
import net.zerocontact.client.renderer.ArmedRaiderRender;

import static net.zerocontact.ZeroContact.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegEventBus {
    @SubscribeEvent
    public static void registerAttr(EntityAttributeCreationEvent event) {
        event.put(ModEntitiesReg.ARMED_RAIDER.get(), ArmedRaider.createAttributes().build());
    }
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event){
        EntityRenderers.register(ModEntitiesReg.ARMED_RAIDER.get(),ArmedRaiderRender::new);
    }

}
