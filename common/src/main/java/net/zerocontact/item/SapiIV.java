package net.zerocontact.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public abstract class SapiIV extends ArmorItem {
    private final Type type;
    private final ArmorMaterial material;


    public SapiIV(ArmorMaterial armorMaterial, Type type, Properties properties) {
        super(armorMaterial, type, properties.defaultDurability(armorMaterial.getDurabilityForType(type)));
        this.material = armorMaterial;
        this.type = type;
    }

    @Override
    public abstract @NotNull Type getType();

    @Override
    public abstract @NotNull ArmorMaterial getMaterial();

    @Override
    public abstract int getDefense();


    @ExpectPlatform
    public static SapiIV create(ArmorMaterial armorMaterial, ArmorItem.Type type, Item.Properties properties, int defense, int absorb, float mass) {
        throw new AssertionError();
    }
}
