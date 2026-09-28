package net.zerocontact.api.armor.modular;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import org.jetbrains.annotations.NotNull;

public interface EquipmentModule extends IEquipmentTypeTag {
    ResourceLocation getModuleTrait();

    @Override
    @NotNull
    default IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.MODULE;
    }
}
