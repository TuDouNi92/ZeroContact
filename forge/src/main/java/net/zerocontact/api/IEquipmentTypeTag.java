package net.zerocontact.api;

import org.jetbrains.annotations.NotNull;

public interface IEquipmentTypeTag {
    enum EquipmentType {
        PLATE_CARRIER("PLATE_CARRIER"),
        ARMOR("ARMOR"),
        UNIFORM_TOP("UNIFORM_TOP"),
        UNIFORM_PANTS("UNIFORM_PANTS"),
        ARMBAND("ARMBAND"),
        HEADSET("HEADSET"),
        BACKPACK("BACKPACK"),
        RIGS("RIGS");
        private final String name;

        EquipmentType(String name) {
            this.name = name;
        }

        public String getTypeId() {
            return name;
        }
    }

    default @NotNull IEquipmentTypeTag.EquipmentType getArmorType(){
        return EquipmentType.ARMOR;
    }
}
