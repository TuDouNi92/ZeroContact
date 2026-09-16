package net.zerocontact.armor.modular.module.headset.item;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.registry.ModuleRegistry;

public class ComtacII extends Headset {
    private static final ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, "textures/models/headset/comtac2.png");
    private static final ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, "geo/headset/comtac2.geo.json");
    private static final ResourceLocation animation = new ResourceLocation("");

    public ComtacII() {
        super("comtac2", 0, texture, model, animation);
        ModuleRegistry.registerCategory(new ResourceLocation(ZeroContact.MOD_ID, "headset_comtac2"), MountCategory.HEADSET);
    }
}
