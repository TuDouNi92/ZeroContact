package net.zerocontact.models;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.entity.ArmedRaider;
import software.bernie.geckolib.model.GeoModel;

public class ArmedRaiderModel extends GeoModel<ArmedRaider> {
    @Override
    public ResourceLocation getModelResource(ArmedRaider armedRaider) {
        return new ResourceLocation(ZeroContact.MOD_ID,"geo/armed_raider.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ArmedRaider armedRaider) {
        return new ResourceLocation(ZeroContact.MOD_ID,"textures/models/armed_raider.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ArmedRaider armedRaider) {
        return null;
    }
}
