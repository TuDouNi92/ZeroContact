package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.IAssetManager;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.loader.ZContentLoader;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class GenerateUniformPantsGeoImpl extends AbstractGenerateGeoCurioItemImpl implements IEquipmentTypeTag, IAssetManager.GeneratableItem {
    public final Set<GenerationRecord<?>> items = new HashSet<>();

    public GenerateUniformPantsGeoImpl(String id, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(id, defaultDurability, texture, model, animation);
    }

    @Override
    public void deserializeItems() {
        LinkedHashMap<? extends ItemGenData, String> itemGenDataList = ZContentLoader.itemGenData;
        itemGenDataList.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof ItemGenData.Armor data && data.equipmentSlot.equals(EquipmentType.UNIFORM_PANTS.getTypeId()))
                .forEach(item -> {
                    ItemGenData.Armor data = (ItemGenData.Armor) item.getKey();
                    String id = data.id;
                    int defaultDurability = data.defaultDurability;
                    ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
                    ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
                    ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
                    items.add(new GenerationRecord<>(id, new GenerateUniformPantsGeoImpl(id, defaultDurability, texture, model, animation), item.getValue()));
                });
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.UNIFORM_PANTS;
    }
}
