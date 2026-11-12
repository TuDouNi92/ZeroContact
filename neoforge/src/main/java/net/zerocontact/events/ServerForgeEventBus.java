package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.command.CommandManager;
import net.zerocontact.effects.ZCEffect;
import net.zerocontact.item.backpack.BaseBackpack;

@EventBusSubscriber(modid = ZeroContact.MOD_ID)
public class ServerForgeEventBus {

    @SubscribeEvent
    public static void RegCommands(RegisterCommandsEvent event) {
        CommandManager.register(event.getDispatcher(),event.getBuildContext());
    }

    public static void regEvents() {
        TickEvent.SERVER_LEVEL_POST.register(ZCEffect.Tick::serverTick);
        dev.architectury.event.events.common.EntityEvent.LIVING_HURT.register(PlateDamageEvent::register);
    }

    @SubscribeEvent
    public static void entityHurtByGunEvent(EntityHurtByGunEvent.Pre event) {
        PlateEntityHurtEvent.modifyDamage(event);
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
