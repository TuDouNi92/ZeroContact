package net.zerocontact.suppression.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.interaction.SuppressionManager;
import net.zerocontact.cofig.ModConfigs;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CameraShake {
    private static float oldSuppression = 0.0f;
    private static float rollAmount = 0;
    private static float shakeAmount = 0;

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!ModConfigs.CLIENT.enableBulletSuppression.get()) return;
        float suppression = SuppressionManager.suppressionLevel;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        double time = (level.getGameTime() + event.getPartialTick()) * 0.05f;

        float decayAmount = 0.00025f;
        if (suppression - oldSuppression > 0) {
            rollAmount = (float) Math.sin(time) * 1.6f * suppression;
            shakeAmount = (float) Math.cos(time) * 0.25f * suppression;
        } else {
            rollAmount = approachZero(rollAmount, decayAmount);
            shakeAmount = approachZero(shakeAmount, decayAmount);
        }
        event.setRoll(event.getRoll() + rollAmount);
        event.setPitch(event.getPitch() + shakeAmount);
        event.setYaw(event.getYaw() + shakeAmount);

        oldSuppression = suppression;
    }

    private static float approachZero(float value, float amount) {
        if (value > 0) {
            return Math.max(0, value - amount);
        }
        return Math.min(0, value + amount);
    }
}
