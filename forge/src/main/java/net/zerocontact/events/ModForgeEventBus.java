package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.entity.EntityKineticBullet;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.interaction.BulletPassBy;
import net.zerocontact.command.ToggleStaminaCommand;
import net.zerocontact.network.ModMessages;
import net.zerocontact.stamina.PlayerStamina;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModForgeEventBus {
    @SubscribeEvent
    public static void onEntity(EntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityKineticBullet) {
            BulletPassBy.playBulletPassBySound(entity, Minecraft.getInstance().player);
        }
    }

    @SubscribeEvent
    public static void RegCommands(RegisterCommandsEvent event) {
        ToggleStaminaCommand.register(event.getDispatcher());
    }

    public static void regEvents() {
        ModMessages.register();
        TickEvent.PLAYER_PRE.register(PlayerStamina::staminaTick);
        dev.architectury.event.events.common.EntityEvent.LIVING_HURT.register((lv, source, amount) -> {
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
