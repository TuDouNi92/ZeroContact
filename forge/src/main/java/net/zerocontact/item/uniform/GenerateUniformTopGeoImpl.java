package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.zerocontact.api.datagen.IAssetManager;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.datagen.model.GenerationRecord;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GenerateUniformTopGeoImpl extends AbstractGenerateGeoCurioItemImpl implements IEquipmentTypeTag, IAssetManager.GeneratableItem {
    public final Set<GenerationRecord<?>> items = new HashSet<>();

    public GenerateUniformTopGeoImpl(String id, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(id, defaultDurability, texture, model, animation, ArmorItem.Type.CHESTPLATE);
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.UNIFORM_TOP;
    }
}
