package net.zerocontact.models;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.item.helmet.Ratnik;
import software.bernie.geckolib.model.GeoModel;

public class RatnikModel extends GeoModel<Ratnik> {
    @Override
    public ResourceLocation getModelResource(Ratnik ratnik) {
        return new ResourceLocation(ZeroContact.MOD_ID,"geo/6b47.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Ratnik ratnik) {
        return new ResourceLocation(ZeroContact.MOD_ID, "textures/models/helmet/6b47.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Ratnik ratnik) {
        return null;
    }
}
