package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class CyanCap extends BaseGeoHelmet{
    private static final ResourceLocation  texture= new ResourceLocation(MOD_ID,"textures/models/helmet/helmet_cap_cyan.png") ;
    private static final ResourceLocation  model= new ResourceLocation(MOD_ID,"geo/helmet_cap_cyan.geo.json") ;
    private static final ResourceLocation  animation= null;
    public CyanCap(int absorb, int defaultDurability) {
        super(absorb, defaultDurability, texture, model, animation);
    }
}
