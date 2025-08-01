package net.zerocontact.models;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.item.armor.forge.AvsArmorImpl;
import software.bernie.geckolib.model.GeoModel;

public class AvsModel extends GeoModel<AvsArmorImpl> {
    @Override
    public ResourceLocation getModelResource(AvsArmorImpl animatable) {
        return new ResourceLocation(ZeroContact.MOD_ID,"geo/vest_avs_tagilla_desert.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AvsArmorImpl armor) {
        return new ResourceLocation(ZeroContact.MOD_ID,"textures/models/armor/vest_avs_tagilla_desert.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AvsArmorImpl armor) {
        return null;
    }
}

