package net.zerocontact.datagen;

import com.google.gson.annotations.SerializedName;

public class ItemGenData {
    public static class Plate extends ItemGenData{
        //Item identifier under namespace of zerocontact
        public String id;
        //The durability
        @SerializedName("durability")
        public int durability;
        //Vanilla armor defense
        public int defense;
        //Protection level,ranges unlimited.
        @SerializedName("protection_class")
        public int protectionClass;
        //Movement fix for the plate, usually at ranges of [-0.01,0.1], but you can make it crazy.
        @SerializedName("movement_fix")
        public float movementFix;
        //Indicates the factor of durability loss when get hit.
        @SerializedName("durability_loss_modifier")
        public float durabilityLossModifier =1;
        //Have to be the Geckolib format resources
        public String texture = "";
        public String model = "";
        public String animation = "";

        //Indicates the factor of damage when get hurt, check the list of variants below
        @SerializedName("hurt_modifier")
        public HurtModifier hurtModifier;
        public static class HurtModifier {
            //The multiplier represents the proportion of the original damage that is applied after mitigation,
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
        @SerializedName("protection_class")
        public int protectionClass;
        @SerializedName("default_durability")
        public int defaultDurability;
        @SerializedName("movement_fix")
        public float movementFix = 0;
        public String texture = "";
        public String model = "";
        public String animation = "";
        @SerializedName("durability_loss_modifier")
        public float durabilityLossModifier =1;
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
    public static class Loadout extends ItemGenData{
        public String id;
        @SerializedName("container_size")
        public int containerSize;
        @SerializedName("equipment_slot")
        public String equipmentSlot;
        public String texture = "";
        public String model = "";
        public String animation = "";
    }
}
