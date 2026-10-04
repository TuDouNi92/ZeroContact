package net.zerocontact.armor.modular.module.headset.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record AudioProfile(
        @SerializedName("compressor_ratio")
        float compressorRatio,
        @SerializedName("transient_attack")
        float transientAttackDb,
        @SerializedName("transient_sustain")
        float transientSustainDb,
        @SerializedName("pick_up_attenuation")
        float pickUpAttenuation,
        @SerializedName("eq")
        List<EqualizerBand> equalizerBands
) {
    public static List<EqualizerBand> defaultEqBands() {
        return List.of(
                new EqualizerBand(125, 0, 1),
                new EqualizerBand(500, 0, 1),
                new EqualizerBand(2000, 0, 1),
                new EqualizerBand(4000, 0, 1),
                new EqualizerBand(8000, 0, 1)
        );
    }
}
