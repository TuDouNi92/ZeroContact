package net.zerocontact.forge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.zerocontact.ZeroContact;
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

    public ZeroContactForge(IEventBus bus, ModContainer container) {
        NeoForge.EVENT_BUS.register(this);
        ZeroContact.init();
        GeckoLib.initialize();
        ServerForgeEventBus.regEvents();
        EntitiyRegistry.register();
        BlockRegistry.register(bus);
        ParticleRegistry.register();
        EffectRegistry.register();
        ZcLuaApiHelpers.register();
        Predicate.predicateCurios();
        EntityDeathDogTagEvent.register();
        regConfig(container);
        regConfigScreen();
        CapabilityRegistries.register();
        ModuleRegistry.registerTrait();
    }

    private static void regConfig(ModContainer container) {

        container.registerConfig(
                ModConfig.Type.COMMON,
                ModConfigs.COMMON_CONFIG_SPEC
        );
        container.registerConfig(
                ModConfig.Type.CLIENT,
                ModConfigs.CLIENT_CONFIG_SPEC
        );
        container.registerConfig(
                ModConfig.Type.SERVER,
                ModConfigs.SERVER_CONFIG_SPEC
        );
    }

    private static void regConfigScreen() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ModConfigScreenHandler.register(context));
    }
}
