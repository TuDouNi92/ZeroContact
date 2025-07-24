package net.zerocontact.forge;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.zerocontact.ZeroContact;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zerocontact.datagen.Predicate;
import net.zerocontact.events.EntityDeathDogTagEvent;
import net.zerocontact.events.EventUtil;
import net.zerocontact.events.PlateDamageEvent;
import net.zerocontact.events.PlateEntityHurtEvent;
import net.zerocontact.forge_registries.ModEntitiesReg;
import net.zerocontact.network.ModMessages;
import net.zerocontact.stamina.PlayerStamina;
import software.bernie.geckolib.GeckoLib;

@Mod(ZeroContact.MOD_ID)
public class ZeroContactForge {
    public ZeroContactForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ZeroContact.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ZeroContact.init();
        GeckoLib.initialize();
        EventRegister.regEvents();
        ModEntitiesReg.register();
        Predicate.predicateCurios();
        EntityDeathDogTagEvent.register();
    }
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    static class EventRegister {
        private static void regEvents() {
            ModMessages.register();
            TickEvent.PLAYER_PRE.register(PlayerStamina::staminaTick);
            EntityEvent.LIVING_HURT.register((lv, source, amount) -> {
                        PlateDamageEvent.DamagePlateRegister(lv, source, amount);
                        if (PlateEntityHurtEvent.changeHurtAmountRicochet(lv, source, amount, EventUtil.idHitFromBack(lv, source))) {
                            return EventResult.interruptFalse();
                        }
                        return EventResult.pass();
                    }
            );
        }
        @SubscribeEvent
        public static void entityHurtByGunEvent(EntityHurtByGunEvent event) {
            PlateEntityHurtEvent.entityHurtByGunHeadShot(event);
            PlateDamageEvent.DamageHelmet(event);
        }
    }

}
