package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;

public class BaseUniformTop extends AbstractGenerateGeoCurioItemImpl implements IEquipmentTypeTag {
    public BaseUniformTop(ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super("", 0, texture, model, animation);
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.UNIFORM_TOP;
    }
}
