package net.zerocontact.armor.modular.module.headset.client.audio;

public final class AudioProcessor {

    /** Stateful, broadband transient shaping. Create one instance per sound, not per channel. */
    public static final class TransientShaper {
        private static final float ENVELOPE_FLOOR = 1.0E-6F;
        private final float attackDb;
        private final float sustainDb;
        private final float fastAttack;
        private final float slowAttack;
        private final float fastRelease;
        private final float slowRelease;
        private final float gainSmoothing;
        private float fastEnvelope;
        private float attackEnvelope;
        private float sustainEnvelope;
        private float smoothedDb;

        public TransientShaper(float sampleRate) {
            this(sampleRate, -6.0F, -6.0F);
        }

        /** Positive dB emphasizes attacks/decaying tails; negative dB softens them. */
        public TransientShaper(float sampleRate, float attackDb, float sustainDb) {
            if (!Float.isFinite(sampleRate) || sampleRate <= 0
                    || !Float.isFinite(attackDb) || Math.abs(attackDb) > 24
                    || !Float.isFinite(sustainDb) || Math.abs(sustainDb) > 24) {
                throw new IllegalArgumentException("Expected positive sample rate and gains within +/-24 dB");
            }
            this.attackDb = attackDb;
            this.sustainDb = sustainDb;
            fastAttack = coefficient(sampleRate, 1.0F);
            slowAttack = coefficient(sampleRate, 20.0F);
            fastRelease = coefficient(sampleRate, 30.0F);
            slowRelease = coefficient(sampleRate, 150.0F);
            gainSmoothing = coefficient(sampleRate, 0.5F);
        }

        public float process(float input) {
            return input * processGain(Math.abs(input));
        }

        /** Call once per PCM frame with the maximum absolute sample across channels. */
        public float processGain(float peak) {
            if (!Float.isFinite(peak) || peak < 0) {
                throw new IllegalArgumentException("Expected a finite nonnegative peak");
            }
            fastEnvelope = follow(peak, fastEnvelope, fastAttack, fastRelease);
            attackEnvelope = follow(peak, attackEnvelope, slowAttack, fastRelease);
            sustainEnvelope = follow(peak, sustainEnvelope, fastAttack, slowRelease);

            float attack = Math.max(0, fastEnvelope - attackEnvelope)
                    / Math.max(fastEnvelope, ENVELOPE_FLOOR);
            float sustain = Math.max(0, sustainEnvelope - fastEnvelope)
                    / Math.max(sustainEnvelope, ENVELOPE_FLOOR);
            float targetDb = attackDb * attack + sustainDb * sustain;
            smoothedDb = gainSmoothing * smoothedDb + (1 - gainSmoothing) * targetDb;
            return (float) Math.pow(10.0, smoothedDb / 20.0);
        }

        private static float follow(float level, float envelope, float attack, float release) {
            float coefficient = level > envelope ? attack : release;
            return coefficient * envelope + (1 - coefficient) * level;
        }

        private static float coefficient(float sampleRate, float milliseconds) {
            return (float) Math.exp(-1000.0 / (sampleRate * (double) milliseconds));
        }

        public static float softClip(float input, float drive, float ceiling) {
            return ceiling * (float) Math.tanh(input * drive / ceiling);
        }
    }

    public static class Compressor {
        private float envelope = 0.0F;

        private  float threshold = 0.20F;
        private  float ratio = 6.0F;

        private final float attackCoeff;
        private final float releaseCoeff;

        public Compressor(float sampleRate) {
            float attackMs = 5.0F;
            float releaseMs = 80.0F;

            attackCoeff = (float) Math.exp(
                    -1.0 /
                            (sampleRate * attackMs / 1000.0)
            );

            releaseCoeff = (float) Math.exp(
                    -1.0 /
                            (sampleRate * releaseMs / 1000.0)
            );
        }

        public float process(float input) {

            float level = Math.abs(input);

            if (level > envelope) {
                envelope =
                        attackCoeff * envelope
                                + (1.0F - attackCoeff) * level;
            } else {
                envelope =
                        releaseCoeff * envelope
                                + (1.0F - releaseCoeff) * level;
            }

            float gain = 1.0F;

            if (envelope > threshold) {

                float compressed =
                        threshold
                                + (envelope - threshold) / ratio;

                gain = compressed / envelope;
            }

            return input * gain;
        }

        public static float simpleVolumeRecompress(float input) {
            float gain = 2.0F;
            float threshold = 0.35F;
            float ratio = 4.0F;

            // 先做耳机增益
            float x = input * gain;

            // 超过阈值后压缩
            if (x > threshold) {
                x = threshold + (x - threshold) / ratio;
            }

            return Math.min(x, 1.0F);
        }

    }
}
