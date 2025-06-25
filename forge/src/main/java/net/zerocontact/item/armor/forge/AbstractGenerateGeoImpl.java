package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.zerocontact.item.PlateBaseMaterial;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

public abstract class AbstractGenerateGeoImpl extends ArmorItem implements GeoItem {
    protected final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    protected final Type type;
    protected final int defense;
    public final String id;
    protected final ResourceLocation texture, model, animation;

    public AbstractGenerateGeoImpl(Type type, String id, int defense, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(PlateBaseMaterial.ARMOR_STEEL, type, new Properties().defaultDurability(defaultDurability));
        this.type = type;
        this.id = id;
        this.defense = defense;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
    }

    @Override
    public @NotNull Type getType() {
        return type;
    }

    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return PlateBaseMaterial.ARMOR_STEEL;
    }

    @Override
    public int getDefense() {
        return defense;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    protected static ArmorItem.Type getArmorType(String equipmentSlot) {
        if (equipmentSlot.equals("HEAD")) {
            return Type.HELMET;
        }
        return Type.CHESTPLATE;
    }
}

