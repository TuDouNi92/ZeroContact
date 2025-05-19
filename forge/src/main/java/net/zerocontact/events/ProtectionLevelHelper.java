package net.zerocontact.events;

import net.minecraft.world.item.ItemStack;

public enum ProtectionLevelHelper {
    NIJIIA(5),
    NIJII(6),
    NIJIIIA(7),
    NIJIII(9),
    NIJIV(13);
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
