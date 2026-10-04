package net.zerocontact.armor.modular.module.headset.client.audio;

import net.zerocontact.armor.modular.module.headset.model.EqualizerBand;

import java.util.List;

/** Stateful EQ: one instance per sound, with independent channel histories. */
public final class MultiBandEqualizer {
    private final Biquad[] filters;

    public MultiBandEqualizer(float sampleRate, int channels, List<EqualizerBand> bands) {
        if (!Float.isFinite(sampleRate) || sampleRate <= 0 || channels < 1 || channels > 2) {
            throw new IllegalArgumentException("Expected positive sample rate and mono/stereo channels");
        }
        // Bypass bands outside the representable spectrum and exact unity bands.
        filters = bands.stream()
                .filter(band -> band.gainDb() != 0 && band.frequencyHz() < sampleRate / 2.0)
                .map(band -> new Biquad(sampleRate, channels, band))
                .toArray(Biquad[]::new);
    }

    public boolean isActive() {
        return filters.length != 0;
    }

    public float process(float sample, int channel) {
        double output = sample;
        for (Biquad filter : filters) output = filter.process(output, channel);
        return (float) output;
    }

    private static final class Biquad {
        private final double b0, b1, b2, a1, a2;
        private final double[] z1, z2;

        private Biquad(float sampleRate, int channels, EqualizerBand band) {
            // RBJ peaking EQ: https://www.w3.org/TR/audio-eq-cookbook/
            double a = Math.pow(10, band.gainDb() / 40.0);
            double omega = 2 * Math.PI * band.frequencyHz() / sampleRate;
            double alpha = Math.sin(omega) / (2 * band.q());
            double a0 = 1 + alpha / a;
            b0 = (1 + alpha * a) / a0;
            b1 = -2 * Math.cos(omega) / a0;
            b2 = (1 - alpha * a) / a0;
            a1 = b1;
            a2 = (1 - alpha / a) / a0;
            z1 = new double[channels];
            z2 = new double[channels];
        }

        private double process(double input, int channel) {
            double output = b0 * input + z1[channel];
            z1[channel] = b1 * input - a1 * output + z2[channel];
            z2[channel] = b2 * input - a2 * output;
            return output;
        }
    }
}
