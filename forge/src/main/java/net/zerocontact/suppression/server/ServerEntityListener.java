package net.zerocontact.suppression.server;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.network.AppendSuppressionPacket;
import net.zerocontact.network.ModMessages;
import net.zerocontact.suppression.BulletSuppression;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class ServerEntityListener {
    private static ServerPlayer player = null;

    @SubscribeEvent
    public static void entityEvent(EntityEvent event) {
        tickBulletSuppression(event);
    }

    @SubscribeEvent
    public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer serverPlayer) {
            player = serverPlayer;
        }
    }

    private static void tickBulletSuppression(EntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityKineticBullet && player != null) {
            BulletSuppression.apply(entity, player, (bl) -> ModMessages.sendToPlayer(new AppendSuppressionPacket(bl.position().toVector3f(), .1f), player));
        }
    }
}
