package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.command.CommandManager;
import net.zerocontact.effects.ZCEffect;
import net.zerocontact.item.backpack.BaseBackpack;
import net.zerocontact.network.ModMessages;
import net.zerocontact.stamina.PlayerStamina;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerForgeEventBus {

    @SubscribeEvent
    public static void RegCommands(RegisterCommandsEvent event) {
        CommandManager.register(event.getDispatcher());
    }

    public static void regEvents() {
        ModMessages.register();
        TickEvent.PLAYER_PRE.register(PlayerStamina::staminaTick);
        TickEvent.SERVER_LEVEL_POST.register(ZCEffect.Tick::serverTick);
        dev.architectury.event.events.common.EntityEvent.LIVING_HURT.register(PlateEntityHurtEvent::entityHurtRegister);
        dev.architectury.event.events.common.EntityEvent.LIVING_HURT.register(PlateDamageEvent::register);
    }

    @SubscribeEvent
    public static void entityHurtByGunEvent(EntityHurtByGunEvent event) {
        PlateEntityHurtEvent.entityHurtByGunHeadShot(event);
        PlateDamageEvent.damageHelmet(event);
    }

    @SubscribeEvent
    public static void onPlayerInteractBackpack(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget().blockPosition().distManhattan(event.getEntity().blockPosition()) < 3.0f) {
            Player player = event.getEntity();
            Entity entity = event.getTarget();
            BaseBackpack.openAllysBackpack(player,entity);
        }
    }
}
