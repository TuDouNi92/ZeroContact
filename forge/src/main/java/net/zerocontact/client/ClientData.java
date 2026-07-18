package net.zerocontact.client;

public class ClientData {
    private static float stamina;
    private static boolean enableStamina;
    public static void setStamina(float stamina,boolean enabled) {
        ClientData.stamina = stamina;
        ClientData.enableStamina = enabled;
    }
    public static float getStamina() {
        return stamina;
    }
    public static boolean isEnableStamina(){
        return enableStamina;
    }
}
