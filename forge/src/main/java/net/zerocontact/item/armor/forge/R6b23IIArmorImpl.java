package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class R6b23IIArmorImpl extends BaseArmorGeoImpl{
    private static final ResourceLocation  texture= new ResourceLocation(MOD_ID,"textures/models/armor/vest_6b23_2_flora.png") ;
    private static final ResourceLocation  model= new ResourceLocation(MOD_ID,"geo/vest_6b23_2_flora.geo.json") ;
    private static final ResourceLocation  animation= null;
    public R6b23IIArmorImpl(int defense, int defaultDurability,int absorb, float mass) {
        super(Type.CHESTPLATE, "", defense, defaultDurability,absorb,mass, texture, model, animation);
    }
}
