package net.zerocontact.armor.modular.module.headset.item;

/** One peaking EQ band; positive gain boosts, negative gain cuts. */
public record EqualizerBand(float frequencyHz, float gainDb, float q) {
    public EqualizerBand {
        if (!Float.isFinite(frequencyHz) || frequencyHz <= 0
                || !Float.isFinite(gainDb) || Math.abs(gainDb) > 24
                || !Float.isFinite(q) || q < 0.1F || q > 20) {
            throw new IllegalArgumentException("Expected positive frequency, gain within +/-24 dB and Q within 0.1..20");
        }
    }
}
