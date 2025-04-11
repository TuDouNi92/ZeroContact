package net.zerocontact.forge;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.platform.forge.EventBuses;
import net.zerocontact.ZeroContact;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zerocontact.events.EventUtil;
import net.zerocontact.events.PlateDamageEvent;
import net.zerocontact.events.PlateEntityHurtEvent;
import software.bernie.geckolib.GeckoLib;

@Mod(ZeroContact.MOD_ID)
public class ZeroContactForge {
    public ZeroContactForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ZeroContact.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ZeroContact.init();
        GeckoLib.initialize();
        EntityEvent.LIVING_HURT.register((lv, source, amount) -> {
                    PlateDamageEvent.DamagePlateRegister(lv, source, amount);
                    if (PlateEntityHurtEvent.changeHurtAmountRicochet(lv, source, amount, EventUtil.idHitFromBack(lv, source))) {
                        return EventResult.interruptFalse();
                    }
                    return EventResult.pass();
                }
        );
    }
}
