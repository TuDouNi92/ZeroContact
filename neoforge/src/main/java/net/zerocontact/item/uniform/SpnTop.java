package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class SpnTop extends BaseUniformTop{
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/uniform/uniform_spn_top.png");
    private static final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/uniform/uniform_spn_top.geo.json");
    private static final ResourceLocation animation = ResourceLocation.fromNamespaceAndPath(MOD_ID,"");
    public SpnTop() {
        super(texture, model, animation);
    }
}
