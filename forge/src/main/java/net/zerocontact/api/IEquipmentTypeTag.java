package net.zerocontact.api;

import org.jetbrains.annotations.NotNull;

public interface IEquipmentTypeTag {
    enum EquipmentType {
        PLATE_CARRIER("PLATE_CARRIER"),
        ARMOR("ARMOR"),
        HELMET("HELMET"),
        UNIFORM_TOP("UNIFORM_TOP"),
        UNIFORM_PANTS("UNIFORM_PANTS"),
        ARMBAND("ARMBAND"),
        HEADSET("HEADSET"),
        BACKPACK("BACKPACK"),
        RIGS("RIGS"),
        PLATE("PLATE"),
        AMMO("AMMO");
        private final String name;
        private final String translationValue;
        EquipmentType(String name) {
            this.name = name;
            this.translationValue = "tooltip.zerocontact.armor_category"+"."+name.toLowerCase();
        }

        public String getTypeId() {
            return name;
        }

        public String getTranslationValue() {
            return translationValue;
        }
    }

    default @NotNull IEquipmentTypeTag.EquipmentType getArmorType(){
        return EquipmentType.ARMOR;
    }
}
