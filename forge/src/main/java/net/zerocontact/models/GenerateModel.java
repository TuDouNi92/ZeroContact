package net.zerocontact.models;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.item.armor.forge.AbstractGenerateGeoImpl;
import software.bernie.geckolib.model.GeoModel;

public class GenerateModel<T extends AbstractGenerateGeoImpl> extends GeoModel<T> {
    private final ResourceLocation texture;
    private final ResourceLocation model;
    private final ResourceLocation animation;
    public GenerateModel(ResourceLocation texture, ResourceLocation model, ResourceLocation animation){
        this.texture = texture;
        this.model =model;
        this.animation = animation;
    }
    @Override
    public ResourceLocation getModelResource(T generateArmor) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(T generateArmor) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(T generateArmor) {
        return animation;
    }
}
