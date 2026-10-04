package net.zerocontact.datagen.model;

import com.google.gson.annotations.SerializedName;
import net.zerocontact.armor.modular.module.headset.model.AudioProfile;

public class ModularHeadsetPOJO extends ModularPOJO {
    @SerializedName("audio_profile")
    public AudioProfile audioProfile = defaultProfile();

    public ModularHeadsetPOJO() {
        audioProfile = audioProfile == null ? defaultProfile() : audioProfile;
    }

    private static AudioProfile defaultProfile() {
        return new AudioProfile(
                0.0f,
                0.0f,
                0.0f,
                16f,
                AudioProfile.defaultEqBands()
        );
    }
}
