package net.zerocontact.datagen.model;

import com.google.gson.annotations.SerializedName;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.model.MountType;

import java.util.*;
import java.util.stream.Collectors;

public class ItemPOJO {
    public static class Plate extends ItemPOJO {
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
        public float durabilityLossModifier = 1;
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
            public Float ricochetMultiplier = 0.05f;
            @SerializedName("penetrate_multiplier")
            public Float penetrateMultiplier = 0.7f;
            @SerializedName("blunt_multiplier")
            public Float bluntMultiplier = 0.1f;
        }
    }

    public static class Armor extends ItemPOJO {
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
        public float durabilityLossModifier = 1;

        @SerializedName("immune_effects")
        public List<String> immuneEffects = List.of();

        @SerializedName("hurt_modifier")
        public Armor.HurtModifier hurtModifier = new HurtModifier();

        public static class HurtModifier {
            @SerializedName("ricochet_multiplier")
            public Float ricochetMultiplier = 0.05f;
            @SerializedName("penetrate_multiplier")
            public Float penetrateMultiplier = 0.7f;
            @SerializedName("blunt_multiplier")
            public Float bluntMultiplier = 0.1f;
        }

        public List<Attachments> attachments = new ArrayList<>();

        public static class Attachments {

            @SerializedName("mount_id")
            public String mountId;

            @SerializedName("mount_type")
            public String mountType;

            @SerializedName("mount_bone")
            public String mountBone;

            @SerializedName("accept_categories")
            public List<String> acceptCategories;

            public MountType getMountType() {
                return Arrays.stream(MountType.values())
                        .filter(e -> e.name().equals(mountType.toUpperCase()))
                        .findFirst()
                        .orElse(MountType.UNDEFINED);
            }

            public Set<MountCategory> getAcceptCategories() {
                HashSet<MountCategory> moduleCategories = new HashSet<>();
                if (acceptCategories.isEmpty()) {
                    moduleCategories.add(MountCategory.UNDEFINED);
                } else {
                    moduleCategories = acceptCategories.stream()
                            .map(str -> Arrays.stream(MountCategory.values())
                                    .filter(category -> category.name().equals(str.toUpperCase()))
                                    .findFirst()
                                    .orElse(MountCategory.UNDEFINED)
                            )
                            .collect(Collectors.toCollection(HashSet::new));
                }
                return moduleCategories;
            }

        }

        public Armor() {
            immuneEffects = immuneEffects == null ? List.of() : immuneEffects;
            attachments = attachments == null ? List.of() : attachments;
        }
    }

    public static class Loadout extends ItemPOJO {
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
