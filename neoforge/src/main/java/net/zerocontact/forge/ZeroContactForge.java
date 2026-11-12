package net.zerocontact.forge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.armor.modular.component.ModularDataComponents;
import net.zerocontact.armor.modular.capability.ModularCapabilityRegistry;
import net.zerocontact.config.ModConfigs;
import net.zerocontact.datagen.Predicate;
import net.zerocontact.events.*;
import net.zerocontact.forge_registries.*;
import net.zerocontact.lua.ZcLuaApiHelpers;
import net.zerocontact.network.ModMessages;

@Mod(ZeroContact.MOD_ID)
public class ZeroContactForge {

    public ZeroContactForge(IEventBus bus, ModContainer container) {
        bus.addListener(ModMessages::register);
        ModularDataComponents.register(bus);
        bus.addListener(ModularCapabilityRegistry::registerCapabilities);
        ZeroContact.init();
        ServerForgeEventBus.regEvents();
        EntitiyRegistry.register();
        BlockRegistry.register(bus);
        ParticleRegistry.register();
        EffectRegistry.register();
        ZcLuaApiHelpers.register();
        Predicate.predicateCurios();
        regConfig(container);
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

}
