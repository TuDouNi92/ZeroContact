package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Untar extends BaseGeoHelmet{
    private static final ResourceLocation  texture= new ResourceLocation(MOD_ID,"textures/models/helmet/helmet_untar_blue.png") ;
    private static final ResourceLocation  model= new ResourceLocation(MOD_ID,"geo/helmet/helmet_untar_blue.geo.json") ;
    private static final ResourceLocation  animation= null;
    public Untar(int absorb, int defaultDurability) {
        super(absorb, defaultDurability, texture, model, animation);
    }
}
