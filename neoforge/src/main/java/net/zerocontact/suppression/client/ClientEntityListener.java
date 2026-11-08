package net.zerocontact.suppression.client;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.interaction.SuppressionManager;
import net.zerocontact.registries.ModSoundEventsReg;
import net.zerocontact.suppression.BulletSuppression;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEntityListener {
    @SubscribeEvent
    public static void entityEvent(EntityEvent event) {
        tickBulletSuppression(event);
    }

    private static void tickBulletSuppression(EntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityKineticBullet) {
            BulletSuppression.apply(entity, Minecraft.getInstance().player,
                    (bl) -> SuppressionManager.increaseSuppression(.1f,
                            () -> bl.playSound(ModSoundEventsReg.randomBulletSound(), 2.0f, 1.0f)));
        }
    }
}
