package net.zerocontact.client;

public class ClientData {
    private static float stamina;
    private static boolean lastToggleVisorEnabled =false;
    private static boolean triggerVisorToggle;
    private static boolean triggerBackPackToggle;
    public static  boolean justCloseBackpack=false;
    public static void setTriggerBackPackToggle(boolean triggerBackPackToggle) {
        ClientData.triggerBackPackToggle = triggerBackPackToggle;
    }
    public static void setStamina(float stamina) {
        ClientData.stamina = stamina;
    }

    public static float getStamina() {
        return stamina;
    }
    public static boolean isLastToggleVisorEnabled() {
        return lastToggleVisorEnabled;
    }
    public static boolean isTriggerVisorToggle(){
        return triggerVisorToggle;
    }
    public static void setTriggerViosorToggle(boolean bool){
        triggerVisorToggle = bool;
    }
    public static void setLastToggleVisorEnabled(boolean toggleVisorSuccess) {
        lastToggleVisorEnabled = toggleVisorSuccess;
    }
}
