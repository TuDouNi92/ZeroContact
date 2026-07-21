package net.zerocontact.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.config.ModConfig;
import net.zerocontact.ZeroContact;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.cofig.ModConfigs;
import net.zerocontact.datagen.Predicate;
import net.zerocontact.events.*;
import net.zerocontact.forge_registries.BlocksRegForge;
import net.zerocontact.forge_registries.ModEntitiesReg;
import software.bernie.geckolib.GeckoLib;

@Mod(ZeroContact.MOD_ID)
public class ZeroContactForge {
    private static FMLJavaModLoadingContext fmlJavaModLoadingContext = null;
    public ZeroContactForge(FMLJavaModLoadingContext context) {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ZeroContact.MOD_ID, context.getModEventBus());
        ZeroContact.init();
        fmlJavaModLoadingContext = context;
        GeckoLib.initialize();
        ServerForgeEventBus.regEvents();
        ModEntitiesReg.register();
        BlocksRegForge.register(context.getModEventBus());
        Predicate.predicateCurios();
        EntityDeathDogTagEvent.register();
        regConfig(context);
        CapabilityRegistries.register();
    }

    private static void regConfig(FMLJavaModLoadingContext context) {
        context.registerConfig(
                ModConfig.Type.COMMON,
                ModConfigs.COMMON_CONFIG_SPEC
        );
        context.registerConfig(
                ModConfig.Type.CLIENT,
                ModConfigs.CLIENT_CONFIG_SPEC
        );
    }

    public static FMLJavaModLoadingContext getFmlJavaModLoadingContext() {
        return fmlJavaModLoadingContext;
    }
}
