package net.zerocontact.armor.modular.module.headset.client.audio;

public final class ClientSoundEngineHandler {

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
