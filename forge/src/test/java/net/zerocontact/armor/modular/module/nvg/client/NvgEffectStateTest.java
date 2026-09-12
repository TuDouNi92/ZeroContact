package net.zerocontact.armor.modular.module.nvg.client;

/** Dependency-free regression checks. Run main with NvgEffectState on the classpath. */
public final class NvgEffectStateTest {
    private static int checks;
    public static void main(String[] args) {
        var state = new NvgEffectState();
        check(!state.wantsShader() && state.alpha(1) == 0, "initially off");
        state.request(true);
        tick(state, 9);
        check(!state.wantsShader() && state.alpha(1) == 0, "wait for readiness");
        tick(state, 4);
        check(!state.wantsShader(), "respect five-tick readiness delay");
        tick(state, 1);
        check(state.wantsShader(), "fallback enables without render callbacks");
        tick(state, 6);
        check(state.phase() == NvgEffectState.Phase.ON, "activation settles");

        state.request(false);
        state.marker("nvg_ready;");
        tick(state, 5);
        check(state.wantsShader(), "retain effect until hidden marker");
        tick(state, 1);
        check(!state.wantsShader(), "hidden fallback disables effect");
        tick(state, 14);
        check(state.phase() == NvgEffectState.Phase.OFF && state.alpha(1) == 0, "deactivation settles");

        state.request(true);
        state.marker(" nvg_ready; ");
        tick(state, 5);
        check(state.wantsShader(), "semicolon marker handled before fallback");
        state.tick(true);
        check(!state.wantsShader(), "power loss disables immediately");

        state.request(false);
        state.marker("nvg_ready;");
        state.marker("nvg_hidden;");
        tick(state, 1);
        check(!state.wantsShader(), "old ready marker cannot reopen during deactivation");
        state.request(true);
        state.marker("nvg_hidden;");
        tick(state, 20);
        check(state.wantsShader() && state.phase() == NvgEffectState.Phase.ON, "rapid reversal converges");
        state.reset();
        check(state.phase() == NvgEffectState.Phase.OFF && !state.wantsShader()
                && state.alpha(0.5F) == 0, "reset clears interpolation and effects");

        state.request(true);
        tick(state, 3);
        state.request(false);
        tick(state, 20);
        check(!state.wantsShader() && state.alpha(1) == 0, "cancel before readiness never opens");
        System.out.println("NvgEffectState: " + checks + " regression checks passed");
    }

    private static void tick(NvgEffectState state, int count) {
        for (int i = 0; i < count; i++) state.tick(false);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
