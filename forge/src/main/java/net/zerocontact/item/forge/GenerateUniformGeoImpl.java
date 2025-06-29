package net.zerocontact.item.forge;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.ArmorTypeTag;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.loader.ItemLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GenerateUniformGeoImpl extends AbstractGenerateGeoCurioItemImpl implements ArmorTypeTag.Uniform {
    public static Set<GenerateUniformGeoImpl> items = new HashSet<>();

    public GenerateUniformGeoImpl(String id, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(id, defaultDurability, texture, model, animation);
    }

    public static void regItems() {
        ArrayList<ItemGenData> itemGenDataList = ItemLoader.itemGenData;
        if (itemGenDataList.isEmpty()) return;
        for (ItemGenData data0 : itemGenDataList) {
            if (!(data0 instanceof ItemGenData.Armor data)) continue;
            ZeroContactLogger.LOG.info(new Gson().toJson(data0));
            String id = data.id;
            int defaultDurability = data.defaultDurability;
            ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
            ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
            ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
            if (!(data.equipmentSlot).equals(Type.UNIFORM.getName())) continue;
            items.add(new GenerateUniformGeoImpl(id, defaultDurability, texture, model, animation));
        }
    }

}
