package net.zerocontact.models;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.item.forge.Helmet;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

public class HelmetModel extends GeoModel<Helmet> {
    @Override
    public ResourceLocation getModelResource(Helmet helmet) {
        return new ResourceLocation(ZeroContact.MOD_ID,"geo/fast_mt.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Helmet helmet) {
        return new ResourceLocation(ZeroContact.MOD_ID,"textures/models/helmet/fast_mt.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Helmet helmet) {
        return null;
    }
}
