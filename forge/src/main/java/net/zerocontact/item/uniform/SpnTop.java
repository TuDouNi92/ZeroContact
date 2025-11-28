package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class SpnTop extends BaseUniformTop{
    private static final ResourceLocation texture = new ResourceLocation(MOD_ID,"textures/item/uniform/uniform_spn_top.png");
    private static final ResourceLocation model = new ResourceLocation(MOD_ID,"geo/uniform/uniform_spn_top.geo.json");
    private static final ResourceLocation animation = new ResourceLocation(MOD_ID,"");
    public SpnTop() {
        super(texture, model, animation);
    }
}
