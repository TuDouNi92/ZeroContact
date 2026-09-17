package net.zerocontact.armor.modular.module.headset.client.audio;

import com.tacz.guns.client.sound.GunSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;

public final class AudioBehaviorManager {
    private static AudioBehaviorCalc audioBehaviorCalc;

    public static AudioBehaviorCalc getAudioBehaviorCalc() {
        if (audioBehaviorCalc == null) {
            audioBehaviorCalc = new AudioBehaviorCalc();
        }
        return audioBehaviorCalc;
    }

    public static class AudioBehaviorCalc {

        public float increaseLinearAttenuation(float originalAttenuation) {
            if (!HeadsetAudioState.isActive()) {
                return originalAttenuation;
            }
            return originalAttenuation * 2;
        }

        public float overrideGunSoundMinuend() {
            return 0.7175f;
        }

        public double expandSoundRadius(double original) {
            return original * 2;
        }

        public boolean shouldAttachToPCM(SoundInstance instance){
            Sound sound = instance.getSound();
            ResourceLocation soundLocation = sound.getLocation();
            return instance instanceof GunSoundInstance || soundLocation.getPath().endsWith(".step");
        }
    }
}
