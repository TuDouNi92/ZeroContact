package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.api.IAssetManager;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GenerateUniformTopGeoImpl extends AbstractGenerateGeoCurioItemImpl implements IEquipmentTypeTag, IAssetManager.GeneratableItem {
    public final Set<GenerationRecord<?>> items = new HashSet<>();

    public GenerateUniformTopGeoImpl(String id, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(id, defaultDurability, texture, model, animation);
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.UNIFORM_TOP;
    }
}
