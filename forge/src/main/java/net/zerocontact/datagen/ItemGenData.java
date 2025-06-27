package net.zerocontact.datagen;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

public class ItemGenData {
    public static class Plate extends ItemGenData{
        public String id;
        @SerializedName("language_name")
        public String languageName;
        public int defense;
        public int absorb;
        @SerializedName("movement_fix")
        public float movementFix;
        @SerializedName("durability_loss_modifier")
        public int durabilityLossModifier =1;
        @SerializedName("hurt_modifier")
        public HurtModifier hurtModifier;
        public static class HurtModifier {
            @SerializedName("ricochet_multiplier")
            public  Float ricochetMultiplier =0.05f;
            @SerializedName("penetrate_multiplier")
            public  Float penetrateMultiplier =0.7f;
            @SerializedName("blunt_multiplier")
            public  Float bluntMultiplier = 0.1f;
        }
    }
    public static class Armor extends ItemGenData{
        public String id;
        @SerializedName("equipment_slot")
        public String equipmentSlot;
        public int defense;
        public int absorb;
        @SerializedName("default_durability")
        public int defaultDurability;
        public String texture;
        public String model;
        public String animation;
        @SerializedName("movement_fix")
        public float movementFix;
        @SerializedName("durability_loss_modifier")
        public int durabilityLossModifier =1;
        @SerializedName("hurt_modifier")
        public Armor.HurtModifier hurtModifier = new HurtModifier();
        public static class HurtModifier {
            @SerializedName("ricochet_multiplier")
            public  Float ricochetMultiplier =0.05f;
            @SerializedName("penetrate_multiplier")
            public  Float penetrateMultiplier =0.7f;
            @SerializedName("blunt_multiplier")
            public  Float bluntMultiplier =0.1f;
        }
    }
}
