package net.zerocontact.client.interaction;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class SuppressionManager {
    public static float suppressionLevel = 0;
    public static final float DECAY_RATE = 0.0065f;

    public static void increaseSuppression(float amount) {
        suppressionLevel = (float) Math.min(1.0, suppressionLevel + amount);
    }

    public static void decreaseSuppression() {
        suppressionLevel = (float) Math.max(0.0, suppressionLevel - DECAY_RATE);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    static class Listener {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            if (suppressionLevel > 0) {
                decreaseSuppression();
            }
        }
    }
}
