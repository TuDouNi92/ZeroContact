package net.zerocontact.events;

public enum ProtectionLevel {
    NIJIIA(5),
    NIJII(6),
    NIJIIIA(7),
    NIJIII(9),
    NIJIV(13);
    private int protectionAmount;
    ProtectionLevel(int amount){
        this.protectionAmount = amount;
    }
    public static ProtectionLevel getProtectionLevel(int amount){
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
