package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Bastion extends BaseGeoHelmet {
    private static final ResourceLocation  texture= new ResourceLocation(MOD_ID,"textures/models/helmet/helmet_bastion_black.png") ;
    private static final ResourceLocation  model= new ResourceLocation(MOD_ID,"geo/helmet_bastion_black.geo.json") ;
    private static final ResourceLocation  animation= null;
    public Bastion(int absorb,int durability) {
        super(absorb,durability,texture,model,animation);
    }
}
