package net.zerocontact.forge.items;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.zerocontact.SapiIV;

public class SapiIVFactoryImpl {
    public static SapiIV create(ArmorMaterial armorMaterial, ArmorItem.Type type, Item.Properties properties) {
        return new SapiIVImpl(armorMaterial, type, properties);
    }
}
