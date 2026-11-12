package net.zerocontact.suppression.client;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.client.interaction.SuppressionManager;
import net.zerocontact.registries.ModSoundEventsReg;
import net.zerocontact.suppression.BulletSuppression;

@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
public class ClientEntityListener {
    @SubscribeEvent
    public static void entityEvent(EntityTickEvent.Post event) {
        tickBulletSuppression(event);
    }

    private static void tickBulletSuppression(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityKineticBullet) {
            BulletSuppression.apply(entity, Minecraft.getInstance().player,
                    (bl) -> SuppressionManager.increaseSuppression(.1f,
                            () -> bl.playSound(ModSoundEventsReg.randomBulletSound(), 2.0f, 1.0f)));
        }
    }
}
