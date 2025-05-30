package net.zerocontact.datagen;

import com.google.gson.annotations.SerializedName;

public class ItemGenData {
    public String id;
    @SerializedName("language_name")
    public String languageName;
    public int defense;
    public int absorb;
    @SerializedName("movement_fix")
    public float movementFix;
    @SerializedName("durability_loss_modifier")
    public int durabilityLossModifier;
    @SerializedName("hurt_modifier")
    public HurtModifier hurtModifier;
    public static class HurtModifier {
        @SerializedName("ricochet_multiplier")
        public  Float ricochetMultiplier;
        @SerializedName("penetrate_multiplier")
        public  Float penetrateMultiplier;
        @SerializedName("blunt_multiplier")
        public  Float bluntMultiplier;
    }
}
