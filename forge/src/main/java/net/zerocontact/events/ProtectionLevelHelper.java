package net.zerocontact.events;

import net.minecraft.world.item.ItemStack;

public enum ProtectionLevelHelper {
    NIJIIA(3),
    NIJII(4),
    NIJIIIA(5),
    NIJIII(7),
    NIJIV(12);
    private final int protectionAmount;
    ProtectionLevelHelper(int amount){
        this.protectionAmount = amount;
    }
    public static ProtectionLevelHelper get(int amount){
        if(amount<=NIJIIA.protectionAmount){
            return NIJIIA;
        }
        else if(amount<=NIJII.protectionAmount){
            return NIJII;
        }
        else if(amount <= NIJIIIA.protectionAmount){
            return NIJIIIA;
        }
        else if(amount<= NIJIII.protectionAmount){
            return NIJIII;
        }
        else if(amount<= NIJIV.protectionAmount){
            return NIJIV;
        }
        else{
            return NIJIV;
        }
    }
}
