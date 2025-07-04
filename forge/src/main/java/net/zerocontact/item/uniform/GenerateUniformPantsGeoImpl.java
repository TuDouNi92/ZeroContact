package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.ArmorTypeTag;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.loader.ItemLoader;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GenerateUniformPantsGeoImpl extends AbstractGenerateGeoCurioItemImpl implements ArmorTypeTag {
    public static Set<GenerationRecord> items = new HashSet<>();
    public GenerateUniformPantsGeoImpl(String id, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(id, defaultDurability, texture, model, animation);
    }
    public static void deserializeItems() {
        ArrayList<ItemGenData> itemGenDataList = ItemLoader.itemGenData;
        if (itemGenDataList.isEmpty()) return;
        for (ItemGenData data0 : itemGenDataList) {
            if (!(data0 instanceof ItemGenData.Armor data)) continue;
            String id = data.id;
            int defaultDurability = data.defaultDurability;
            ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
            ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
            ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
            if (!(data.equipmentSlot).equals(ArmorType.UNIFORM_PANTS.getTypeId())) continue;
            items.add(new GenerationRecord(id,new GenerateUniformPantsGeoImpl(id, defaultDurability, texture, model, animation)));
        }
    }

    @Override
    public @NotNull ArmorType getArmorType() {
        return ArmorType.UNIFORM_PANTS;
    }
}
