package net.zerocontact.api;

import org.jetbrains.annotations.NotNull;

public interface ArmorTypeTag {
    enum ArmorType {
        ARMOR("ARMOR"),
        UNIFORM_TOP("UNIFORM_TOP"),
        UNIFORM_PANTS("UNIFORM_PANTS"),
        ARMBAND("ARMBAND"),
        HEADSET("HEADSET"),
        BACKPACK("BACKPACK");
        private final String name;

        ArmorType(String name) {
            this.name = name;
        }

        public String getTypeId() {
            return name;
        }
    }

    default @NotNull ArmorType getArmorType(){
        return ArmorType.ARMOR;
    }
}
