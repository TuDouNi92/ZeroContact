package net.zerocontact.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public abstract class Ceramic extends SapiIV{
    public Ceramic(ArmorMaterial armorMaterial, Type type, Properties properties) {
        super(armorMaterial, type, properties);
    }
    @ExpectPlatform
    public static Ceramic create(ArmorMaterial armorMaterial, ArmorItem.Type type, Item.Properties properties, int defense, int absorb, float mass){
        throw new AssertionError();
    }
}
