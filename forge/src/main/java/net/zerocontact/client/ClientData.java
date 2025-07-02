package net.zerocontact.client;

public class ClientData {
    private static float stamina;
    private static boolean lastToggleVisorSuccess =false;
    public static void setStamina(float stamina) {
        ClientData.stamina = stamina;
    }

    public static float getStamina() {
        return stamina;
    }
    public static boolean isLastToggleVisorSuccess() {
        return lastToggleVisorSuccess;
    }

    public static void setLastToggleVisorSuccess(boolean toggleVisorSuccess) {
        lastToggleVisorSuccess = toggleVisorSuccess;
    }
}
