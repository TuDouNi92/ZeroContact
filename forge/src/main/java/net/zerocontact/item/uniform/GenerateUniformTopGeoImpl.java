package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.IAssetManager;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.loader.ZPackManager;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class GenerateUniformTopGeoImpl extends AbstractGenerateGeoCurioItemImpl implements IEquipmentTypeTag, IAssetManager.GeneratableItem {
    public final Set<GenerationRecord<?>> items = new HashSet<>();

    public GenerateUniformTopGeoImpl(String id, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(id, defaultDurability, texture, model, animation);
    }

    public void deserializeItems() {
        LinkedHashMap<? extends ItemGenData, String> itemGenDataList = ZPackManager.itemGenData;
        itemGenDataList.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof ItemGenData.Armor data && data.equipmentSlot.equals(EquipmentType.UNIFORM_TOP.getTypeId()))
                .forEach(item -> {
                    ItemGenData.Armor data = (ItemGenData.Armor) item.getKey();
                    String id = data.id;
                    int defaultDurability = data.defaultDurability;
                    ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
                    ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
                    ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
                    items.add(new GenerationRecord<>(id, new GenerateUniformTopGeoImpl(id, defaultDurability, texture, model, animation), item.getValue()));
                });
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.UNIFORM_TOP;
    }
}
