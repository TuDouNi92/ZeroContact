package net.zerocontact.armor.modular.module.headset.model;

import com.google.gson.annotations.SerializedName;

/** One peaking EQ band; positive gain boosts, negative gain cuts. */
public record EqualizerBand(
        @SerializedName("freq_hz")
        float frequencyHz,
        @SerializedName("gain")
        float gainDb,
        @SerializedName("precision")
        float q) {
    public EqualizerBand {
        if (!Float.isFinite(frequencyHz) || frequencyHz <= 0
                || !Float.isFinite(gainDb) || Math.abs(gainDb) > 24
                || !Float.isFinite(q) || q < 0.1F || q > 20) {
            throw new IllegalArgumentException("Expected positive frequency, gain within +/-24 dB and Q within 0.1..20");
        }
    }
}
