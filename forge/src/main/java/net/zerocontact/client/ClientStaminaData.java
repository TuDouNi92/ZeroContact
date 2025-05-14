package net.zerocontact.client;

public class ClientStaminaData {
    private static float stamina;

    public static void setStamina(float stamina) {
        ClientStaminaData.stamina = stamina;
    }

    public static float getStamina() {
        return stamina;
    }
}
