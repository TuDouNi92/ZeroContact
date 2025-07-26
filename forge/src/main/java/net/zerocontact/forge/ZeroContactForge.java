package net.zerocontact.forge;

import dev.architectury.platform.forge.EventBuses;
import net.zerocontact.ZeroContact;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
        ModForgeEventBus.regEvents();
        ModEntitiesReg.register();
        Predicate.predicateCurios();
        EntityDeathDogTagEvent.register();
    }
}
