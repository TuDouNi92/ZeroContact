package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ColdFearMask extends BaseGeoHelmet{
    private static final ResourceLocation  texture= new ResourceLocation(MOD_ID,"textures/models/helmet/mask_cold_fear_black.png") ;
    private static final ResourceLocation  model= new ResourceLocation(MOD_ID,"geo/mask_cold_fear_black.geo.json") ;
    private static final ResourceLocation  animation= null;
    public ColdFearMask(int absorb, int defaultDurability) {
        super(absorb, defaultDurability, texture, model, animation);
    }
}
