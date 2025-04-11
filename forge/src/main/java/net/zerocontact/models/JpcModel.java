package net.zerocontact.models;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.item.armor.forge.JpcArmorImpl;
import software.bernie.geckolib.model.GeoModel;

public class JpcModel extends GeoModel<JpcArmorImpl> {

    @Override
    public ResourceLocation getModelResource(JpcArmorImpl sapiIV) {
        return new ResourceLocation(ZeroContact.MOD_ID,"geo/jpc_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JpcArmorImpl sapiIV) {
        return new ResourceLocation(ZeroContact.MOD_ID,"textures/models/armor/jpc.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JpcArmorImpl sapiIV) {
        return null;
    }
}
