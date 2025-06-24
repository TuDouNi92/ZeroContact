package net.zerocontact.models;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.item.armor.forge.GenerateArmorImpl;
import software.bernie.geckolib.model.GeoModel;

public class GenerateModel extends GeoModel<GenerateArmorImpl> {
    private final ResourceLocation texture;
    private final ResourceLocation model;
    private final ResourceLocation animation;
    public GenerateModel(ResourceLocation texture, ResourceLocation model, ResourceLocation animation){
        this.texture = texture;
        this.model =model;
        this.animation = animation;
    }
    @Override
    public ResourceLocation getModelResource(GenerateArmorImpl generateArmor) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(GenerateArmorImpl generateArmor) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(GenerateArmorImpl generateArmor) {
        return animation;
    }
}
