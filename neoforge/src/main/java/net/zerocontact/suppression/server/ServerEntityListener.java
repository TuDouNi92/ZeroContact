package net.zerocontact.suppression.server;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.network.s2c.AppendSuppressionPacket;
import net.zerocontact.network.ModMessages;
import net.zerocontact.suppression.BulletSuppression;

@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerEntityListener {

    @SubscribeEvent
    public static void entityEvent(EntityTickEvent.Post event) {
        tickBulletSuppression(event);
    }


    private static void tickBulletSuppression(EntityTickEvent.Post event) {
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
