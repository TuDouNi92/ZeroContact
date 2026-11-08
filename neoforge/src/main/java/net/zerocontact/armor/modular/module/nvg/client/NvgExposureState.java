package net.zerocontact.armor.modular.module.nvg.client;

/** Tick-driven gain control with separate indoor, outdoor-night and daylight behavior. */
public final class NvgExposureState {
    private static final float DEFAULT_GAIN = 12.0F;
    private static final float MIN_GAIN = 4F;
    private static final float INDOOR_MAX_GAIN = 48.0F;
    private static final float OUTDOOR_MAX_GAIN = 12.0F;
    private static final float OUTDOOR_NIGHT_MIN_GAIN = 6.0F;
    private static final float MAX_BLOCK_LIGHT = 15.0F;
    private static final float BLOCK_LIGHT_FALLOFF_LEVEL = 5.0F;
    private static final float RESPONSE_CURVE = 4.0F;
    private static final float TICK_SECONDS = 0.05F;
    private static final float BRIGHT_ADAPT_SECONDS = 1F;
    private static final float DARK_ADAPT_SECONDS = 1F;
    // ClientLevel.getSkyDarken(float) returns 0.2 at night and 1.0 in clear daylight.
    private static final float NIGHT_SKY_BRIGHTNESS = 0.2F;

    private float gain = DEFAULT_GAIN;
    private float previousGain = DEFAULT_GAIN;
    private boolean initialized;

    /**
     * blockLight is the raw 0..15 block-light level; exposedToSky describes the eye position.
     * skyBrightness is the current client sky factor, not the raw sky-light level (often 15 at night).
     */
    public void tick(float blockLight, boolean exposedToSky, float skyBrightness) {
        previousGain = gain;
        if (!Float.isFinite(blockLight) || !Float.isFinite(skyBrightness)) {
            return;
        }
        float block = Math.max(0.0F, Math.min(MAX_BLOCK_LIGHT, blockLight));
        // Preserve weak-light gain, then fall sharply around block-light level five.
        float darkness = (float) Math.exp(-Math.pow(block / BLOCK_LIGHT_FALLOFF_LEVEL, RESPONSE_CURVE));
        float target;
        if (exposedToSky) {
            // Limit the target only; retain current/previous gain so leaving a dark
            // room uses the same timed adaptation and render interpolation as lighting changes.
            float daylight = clamp01((skyBrightness - NIGHT_SKY_BRIGHTNESS)
                    / (1.0F - NIGHT_SKY_BRIGHTNESS));
            // Nearby lamps reduce gain at night without collapsing it to the daytime minimum.
            float nightGain = OUTDOOR_NIGHT_MIN_GAIN
                    + (OUTDOOR_MAX_GAIN - OUTDOOR_NIGHT_MIN_GAIN) * darkness;
            target = nightGain + (MIN_GAIN - nightGain) * daylight;
        } else {
            // A roof removes the daylight response: room illumination comes from block light.
            target = MIN_GAIN + (INDOOR_MAX_GAIN - MIN_GAIN) * darkness;
        }
        if (!initialized) {
            // Start at the actual environment rather than fading from a stale exposure.
            previousGain = gain = target;
            initialized = true;
            return;
        }
        float seconds = target < gain ? BRIGHT_ADAPT_SECONDS : DARK_ADAPT_SECONDS;
        float blend = 1.0F - (float) Math.exp(-TICK_SECONDS / seconds);
        gain += (target - gain) * blend;
    }

    public float gain(float partialTick) {
        float partial = Math.max(0.0F, Math.min(1.0F, partialTick));
        return previousGain + (gain - previousGain) * partial;
    }

    public void reset() {
        initialized = false;
        previousGain = gain = DEFAULT_GAIN;
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
