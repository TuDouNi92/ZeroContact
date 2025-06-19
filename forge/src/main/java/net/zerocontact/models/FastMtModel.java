package net.zerocontact.models;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.item.helmet.FastMt;
import software.bernie.geckolib.model.GeoModel;

public class FastMtModel extends GeoModel<FastMt> {
    @Override
    public ResourceLocation getModelResource(FastMt helmet) {
        return new ResourceLocation(ZeroContact.MOD_ID,"geo/fast_mt.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FastMt helmet) {
        return new ResourceLocation(ZeroContact.MOD_ID,"textures/models/helmet/fast_mt.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FastMt helmet) {
        return null;
    }
}
