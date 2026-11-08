package net.zerocontact.armor.modular.module.nvg.client;

/** Tick-driven state, independent of whether GeckoLib is rendered this frame. */
public final class NvgEffectState {
    public enum Phase { OFF, ACTIVATING, ON, DEACTIVATING }
    // Match nvg_pvs31.animation.json: .5s ready, .2917s hidden, .9583s duration.
    private static final int READY_TICK = 10;
    private static final int HIDDEN_TICK = 6;
    private static final int DURATION_TICKS = 20;
    private static final int VIGNETTE_DELAY = 10;
    private static final float SHADER_ALPHA_THRESHOLD = 0.7F;
    private static final float FADE_STEP = 0.2F;
    private Phase phase = Phase.OFF;
    private boolean desiredEnabled;
    private boolean visible;
    private boolean powerLost;
    private int elapsed;
    private int visibleTicks;
    private float alpha;
    private float previousAlpha;

    public void request(boolean enabled) {
        desiredEnabled = enabled;
        phase = enabled ? Phase.ACTIVATING : Phase.DEACTIVATING;
        elapsed = 0;
        // Preserve current alpha on reversal, but never reuse an activation marker.
        if (enabled) {
            visible = false;
            visibleTicks = 0;
        }
    }

    public void marker(String instructions) {
        for (String instruction : instructions.split(";")) {
            if (instruction.trim().equals("nvg_ready") && phase == Phase.ACTIVATING) visible = true;
            if (instruction.trim().equals("nvg_hidden") && phase == Phase.DEACTIVATING) visible = false;
        }
    }

    public void tick(boolean outOfPower) {
        powerLost = outOfPower;
        previousAlpha = alpha;
        elapsed = Math.min(DURATION_TICKS, elapsed + 1);
        if (phase == Phase.ACTIVATING) {
            if (elapsed >= READY_TICK) visible = true;
            if (elapsed == DURATION_TICKS) phase = Phase.ON;
        } else if (phase == Phase.DEACTIVATING) {
            if (elapsed >= HIDDEN_TICK) visible = false;
            if (elapsed == DURATION_TICKS) phase = Phase.OFF;
        }
        visibleTicks = visible ? Math.min(VIGNETTE_DELAY, visibleTicks + 1) : 0;
        boolean show = visible && !powerLost;
        alpha = show ? Math.min(1, alpha + FADE_STEP) : Math.max(0, alpha - FADE_STEP);
    }

    public boolean wantsShader() {
        return !powerLost && visible && visibleTicks >= VIGNETTE_DELAY && alpha >= SHADER_ALPHA_THRESHOLD;
    }
    public boolean desiredEnabled() { return desiredEnabled; }
    public Phase phase() { return phase; }
    public float alpha(float partialTick) {
        float partial = Math.max(0, Math.min(1, partialTick));
        return previousAlpha + (alpha - previousAlpha) * partial;
    }
    public void reset() {
        phase = Phase.OFF;
        desiredEnabled = visible = powerLost = false;
        elapsed = visibleTicks = 0;
        alpha = previousAlpha = 0;
    }
}
