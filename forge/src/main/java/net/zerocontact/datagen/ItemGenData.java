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
        public int durabilityLossModifier;
        @SerializedName("hurt_modifier")
        public Armor.HurtModifier hurtModifier;
        public static class HurtModifier {
            @SerializedName("ricochet_multiplier")
            public  Float ricochetMultiplier;
            @SerializedName("penetrate_multiplier")
            public  Float penetrateMultiplier;
            @SerializedName("blunt_multiplier")
            public  Float bluntMultiplier;
        }
    }
}
