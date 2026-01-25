package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class British23 extends BaseGeoHelmet{
    private static final ResourceLocation texture = new ResourceLocation(MOD_ID,"textures/models/helmet/helmet_british23_black.png");
    private static final ResourceLocation model = new ResourceLocation(MOD_ID,"geo/helmet/helmet_british23_black.geo.json");
    private static final ResourceLocation animation = new ResourceLocation(MOD_ID,"");
    public British23(int absorb, int defaultDurability) {
        super(absorb, defaultDurability, texture, model, animation);
    }
}
