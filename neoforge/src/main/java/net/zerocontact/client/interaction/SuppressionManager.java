package net.zerocontact.client.interaction;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.config.ModConfigs;

public class SuppressionManager {
    public static float suppressionLevel = 0;
    public static final float DECAY_RATE = 0.0065f;

    public static void increaseSuppression(float amount, Runnable callback) {
        if (!ModConfigs.CLIENT.enableBulletSuppression().get()) return;
        suppressionLevel = (float) Math.min(1.0, suppressionLevel + amount);
        callback.run();
    }

    public static void decreaseSuppression() {
        suppressionLevel = (float) Math.max(0.0, suppressionLevel - DECAY_RATE);
    }

    @EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
    static class Listener {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            if (suppressionLevel > 0) {
                decreaseSuppression();
            }
        }
    }
}
