package net.zerocontact.armor.modular.module.headset.item;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.EquipmentModule;
import net.zerocontact.armor.modular.module.headset.model.AudioProfile;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;

public class Headset extends AbstractGenerateGeoCurioItemImpl implements EquipmentModule {
    private static final ResourceLocation trait = new ResourceLocation(ZeroContact.MOD_ID, "headset");
    private final AudioProfile profile;

    public Headset(
            String id,
            int defaultDurability,
            ResourceLocation texture, ResourceLocation model, ResourceLocation animation,
            AudioProfile profile) {
        super(id, defaultDurability, texture, model, animation, Type.HELMET);
        this.profile = profile;
    }

    @Override
    public ResourceLocation getModuleTrait() {
        return trait;
    }

    public AudioProfile getAudioProfile() {
        return profile;
    }

}
