package net.zerocontact.events;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.interaction.BulletPassBy;
import net.zerocontact.command.ToggleStaminaCommand;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModForgeEventBus {
    @SubscribeEvent
    public static void onEntity(EntityEvent event) {
        Entity entity = event.getEntity();
        if( entity instanceof EntityKineticBullet){
            BulletPassBy.playBulletPassBySound(entity,Minecraft.getInstance().player);
        }
    }
    @SubscribeEvent
    public static void RegCommands(RegisterCommandsEvent event) {
        ToggleStaminaCommand.register(event.getDispatcher());
    }

}
