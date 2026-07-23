package net.zerocontact.suppression.server;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.network.AppendSuppressionPacket;
import net.zerocontact.network.ModMessages;
import net.zerocontact.suppression.BulletSuppression;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class ServerEntityListener {

    @SubscribeEvent
    public static void entityEvent(EntityEvent event) {
        tickBulletSuppression(event);
    }


    private static void tickBulletSuppression(EntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityKineticBullet && entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.players().forEach(player ->
                    BulletSuppression.apply(
                            entity, player,
                            (bl) -> ModMessages.sendToPlayer(new AppendSuppressionPacket(bl.position().toVector3f(), .1f), player)
                    )
            );
        }
    }
}
