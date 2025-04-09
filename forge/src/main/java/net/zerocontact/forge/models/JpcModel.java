package net.zerocontact.forge.models;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.forge.SapiIVImpl;
import software.bernie.geckolib.model.GeoModel;

public class JpcModel extends GeoModel<SapiIVImpl> {

    @Override
    public ResourceLocation getModelResource(SapiIVImpl sapiIV) {
        return new ResourceLocation(ZeroContact.MOD_ID,"geo/jpc_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SapiIVImpl sapiIV) {
        return new ResourceLocation(ZeroContact.MOD_ID,"textures/models/armor/jpc.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SapiIVImpl sapiIV) {
        return null;
    }
}
