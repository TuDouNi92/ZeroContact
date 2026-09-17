package net.zerocontact.armor.modular.module.headset.item;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.registry.ModuleRegistry;

public class ComtacII extends Headset {
    private static final ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, "geo/headset/comtac2.geo.json");
    private static final ResourceLocation animation = new ResourceLocation("");

    public enum Color{
        OD(new ResourceLocation(ZeroContact.MOD_ID,"textures/models/headset/comtac2.png")),
        CB(new ResourceLocation(ZeroContact.MOD_ID,"textures/models/headset/comtac2_brown.png"));
        Color(ResourceLocation texture){
            this.texture = texture;
        }
        private final ResourceLocation texture;
    }
    public ComtacII(String id,Color color) {
        super("comtac2", 0, color.texture, model, animation);
        ModuleRegistry.registerCategory(new ResourceLocation(ZeroContact.MOD_ID, id), MountCategory.HEADSET);
    }
}
