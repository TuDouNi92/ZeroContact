package net.zerocontact.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.zerocontact.ZeroContact;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zerocontact.client.gui.ConfigScreen;
import net.zerocontact.cofig.ModConfigs;
import net.zerocontact.datagen.Predicate;
import net.zerocontact.events.*;
import net.zerocontact.forge_registries.ModEntitiesReg;
import software.bernie.geckolib.GeckoLib;

@Mod(ZeroContact.MOD_ID)
public class ZeroContactForge {
    public ZeroContactForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ZeroContact.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ZeroContact.init();
        GeckoLib.initialize();
        ServerForgeEventBus.regEvents();
        ModEntitiesReg.register();
        Predicate.predicateCurios();
        EntityDeathDogTagEvent.register();
        regConfig();
    }
    private void regConfig(){
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                ModConfigs.CONFIG_SPEC
        );
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                ()-> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc,parent)->new ConfigScreen(Component.literal("Config screen"),parent)
                )
        );
    }
}
