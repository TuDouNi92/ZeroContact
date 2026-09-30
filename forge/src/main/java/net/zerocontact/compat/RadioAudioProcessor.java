package net.zerocontact.compat;

import java.util.concurrent.ThreadLocalRandom;

/** Stateful processing for 48 kHz, mono, 16-bit voice-chat PCM. */
final class RadioAudioProcessor {
    private static final double SAMPLE_RATE = 48_000.0;
    private static final double COMPRESSOR_THRESHOLD = 0.06;
    private static final double COMPRESSOR_RATIO = 8.0;
    private static final double ATTACK = Math.exp(-1.0 / (SAMPLE_RATE * 0.002));
    private static final double RELEASE = Math.exp(-1.0 / (SAMPLE_RATE * 0.100));
    private static final double MAKEUP_GAIN = 2.0;
    private static final double DRIVE = 3.5;
    private static final double DISTORTION_SCALE = 0.85 / Math.tanh(DRIVE);
    private static final double BUTTERWORTH_Q1 = 0.541196100146197;
    private static final double BUTTERWORTH_Q2 = 1.306562964876377;
    private static final double VOICE_ON_THRESHOLD = 0.012;
    private static final double VOICE_OFF_THRESHOLD = 0.006;
    private static final int VOICE_HOLD_SAMPLES = (int) (SAMPLE_RATE * 0.08);
    private static final double VOICE_ATTACK = Math.exp(-1.0 / (SAMPLE_RATE * 0.005));
    private static final double VOICE_RELEASE = Math.exp(-1.0 / (SAMPLE_RATE * 0.080));
    private static final double NOISE_LEVEL = 0.10;
    private static final double DUCKED_NOISE = 0.20;
    private static final double NOISE_DUCK_ATTACK = Math.exp(-1.0 / (SAMPLE_RATE * 0.010));
    private static final double NOISE_DUCK_RELEASE = Math.exp(-1.0 / (SAMPLE_RATE * 0.120));

    private final Biquad highPass1 = new Biquad(300.0, BUTTERWORTH_Q1, true);
    private final Biquad highPass2 = new Biquad(300.0, BUTTERWORTH_Q2, true);
    private final Biquad lowPass1 = new Biquad(3_000.0, BUTTERWORTH_Q1, false);
    private final Biquad lowPass2 = new Biquad(3_000.0, BUTTERWORTH_Q2, false);
    private final Biquad noiseHighPass = new Biquad(400.0, Math.sqrt(0.5), true);
    private final Biquad outputLowPass1 = new Biquad(3_500.0, BUTTERWORTH_Q1, false);
    private final Biquad outputLowPass2 = new Biquad(3_500.0, BUTTERWORTH_Q2, false);
    private double envelope;
    private double voiceEnvelope;
    private double noiseGain = 1.0;
    private int voiceHoldSamples;
    private boolean voiceDetected;
    private int noiseState = ThreadLocalRandom.current().nextInt() | 1;

    short[] process(short[] pcm) {
        short[] result = new short[pcm.length];
        for (int i = 0; i < pcm.length; i++) {
            double sample = highPass2.process(highPass1.process(pcm[i] / 32768.0));
            sample = lowPass2.process(lowPass1.process(sample));
            double level = Math.abs(sample);
            updateNoiseGain(level);
            double coefficient = level > envelope ? ATTACK : RELEASE;
            envelope = coefficient * envelope + (1.0 - coefficient) * level;
            if (envelope > COMPRESSOR_THRESHOLD) {
                double compressed = COMPRESSOR_THRESHOLD
                        + (envelope - COMPRESSOR_THRESHOLD) / COMPRESSOR_RATIO;
                sample *= compressed / envelope;
            }
            sample = Math.tanh(sample * MAKEUP_GAIN * DRIVE) * DISTORTION_SCALE;
            sample += noiseHighPass.process(nextNoise()) * NOISE_LEVEL * noiseGain;
            sample = outputLowPass2.process(outputLowPass1.process(sample));
            result[i] = (short) Math.round(Math.max(-1.0, Math.min(32767.0 / 32768.0, sample)) * 32768.0);
        }
        return result;
    }

    private void updateNoiseGain(double level) {
        double coefficient = level > voiceEnvelope ? VOICE_ATTACK : VOICE_RELEASE;
        voiceEnvelope = coefficient * voiceEnvelope + (1.0 - coefficient) * level;
        if (voiceEnvelope >= VOICE_ON_THRESHOLD) {
            voiceDetected = true;
            voiceHoldSamples = VOICE_HOLD_SAMPLES;
        } else if (voiceDetected && voiceEnvelope < VOICE_OFF_THRESHOLD) {
            if (voiceHoldSamples > 0) voiceHoldSamples--;
            else voiceDetected = false;
        }
        double target = voiceDetected ? DUCKED_NOISE : 1.0;
        double smoothing = target < noiseGain ? NOISE_DUCK_ATTACK : NOISE_DUCK_RELEASE;
        noiseGain = smoothing * noiseGain + (1.0 - smoothing) * target;
    }

    private double nextNoise() {
        noiseState ^= noiseState << 13;
        noiseState ^= noiseState >>> 17;
        noiseState ^= noiseState << 5;
        return (noiseState >>> 8) / 8388607.5 - 1.0;
    }

    private static final class Biquad {
        private final double b0, b1, b2, a1, a2;
        private double x1, x2, y1, y2;

        private Biquad(double frequency, double q, boolean highPass) {
            double omega = 2.0 * Math.PI * frequency / SAMPLE_RATE;
            double cosine = Math.cos(omega);
            double alpha = Math.sin(omega) / (2.0 * q);
            double a0 = 1.0 + alpha;
            double numerator = (1.0 + (highPass ? cosine : -cosine)) / 2.0;
            b0 = numerator / a0;
            b1 = (highPass ? -2.0 : 2.0) * numerator / a0;
            b2 = b0;
            a1 = -2.0 * cosine / a0;
            a2 = (1.0 - alpha) / a0;
        }

        private double process(double input) {
            double output = b0 * input + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;
            x2 = x1;
            x1 = input;
            y2 = y1;
            y1 = output;
            return output;
        }
    }
}
