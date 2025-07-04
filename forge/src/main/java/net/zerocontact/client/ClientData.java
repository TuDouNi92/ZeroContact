package net.zerocontact.client;

public class ClientData {
    private static float stamina;
    private static boolean lastToggleVisorEnabled =false;
    public static void setStamina(float stamina) {
        ClientData.stamina = stamina;
    }

    public static float getStamina() {
        return stamina;
    }
    public static boolean isLastToggleVisorEnabled() {
        return lastToggleVisorEnabled;
    }

    public static void setLastToggleVisorEnabled(boolean toggleVisorSuccess) {
        lastToggleVisorEnabled = toggleVisorSuccess;
    }
}
