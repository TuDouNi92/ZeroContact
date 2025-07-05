package net.zerocontact.client;

public class ClientData {
    private static float stamina;
    private static boolean lastToggleVisorEnabled =false;
    private static boolean triggerToggle;
    public static void setStamina(float stamina) {
        ClientData.stamina = stamina;
    }

    public static float getStamina() {
        return stamina;
    }
    public static boolean isLastToggleVisorEnabled() {
        return lastToggleVisorEnabled;
    }
    public static boolean isTriggerToggle(){
        return triggerToggle;
    }
    public static void setTriggerToggle(boolean bool){
        triggerToggle = bool;
    }
    public static void setLastToggleVisorEnabled(boolean toggleVisorSuccess) {
        lastToggleVisorEnabled = toggleVisorSuccess;
    }
}
