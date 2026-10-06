package net.zerocontact.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.zerocontact.ZeroContact;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.client.gui.ModConfigScreenHandler;
import net.zerocontact.config.ModConfigs;
import net.zerocontact.datagen.Predicate;
import net.zerocontact.events.*;
import net.zerocontact.forge_registries.*;
import net.zerocontact.lua.ZcLuaApiHelpers;
import software.bernie.geckolib.GeckoLib;

@Mod(ZeroContact.MOD_ID)
public class ZeroContactForge {

    public ZeroContactForge() {
        // Submit our event bus to let architectury register our content on the right time
        FMLJavaModLoadingContext fmlJavaModLoadingContext = FMLJavaModLoadingContext.get();
        EventBuses.registerModEventBus(ZeroContact.MOD_ID, fmlJavaModLoadingContext.getModEventBus());
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        ZeroContact.init();
        GeckoLib.initialize();
        ServerForgeEventBus.regEvents();
        EntitiyRegistry.register();
        BlockRegistry.register(fmlJavaModLoadingContext.getModEventBus());
        ParticleRegistry.register();
        EffectRegistry.register();
        ZcLuaApiHelpers.register();
        Predicate.predicateCurios();
        EntityDeathDogTagEvent.register();
        regConfig(modLoadingContext);
        regConfigScreen(modLoadingContext);
        CapabilityRegistries.register();
        ModuleRegistry.registerTrait();
    }

    private static void regConfig(ModLoadingContext context) {
        context.registerConfig(
                ModConfig.Type.COMMON,
                ModConfigs.COMMON_CONFIG_SPEC
        );
        context.registerConfig(
                ModConfig.Type.CLIENT,
                ModConfigs.CLIENT_CONFIG_SPEC
        );
        context.registerConfig(
                ModConfig.Type.SERVER,
                ModConfigs.SERVER_CONFIG_SPEC
        );
    }

    private static void regConfigScreen(ModLoadingContext context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ModConfigScreenHandler.register(context));
    }
}
