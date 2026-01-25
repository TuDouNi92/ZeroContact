package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class R6b2ArmorImpl extends BaseArmorGeoImpl{
    private static final ResourceLocation  texture= new ResourceLocation(MOD_ID,"textures/models/vest/vest_6b2_flora.png") ;
    private static final ResourceLocation  model= new ResourceLocation(MOD_ID,"geo/vest/vest_6b2_flora.geo.json") ;
    private static final ResourceLocation  animation= null;
    public R6b2ArmorImpl(int defense, int defaultDurability,int absorb, float mass) {
        super(Type.CHESTPLATE, "", defense, defaultDurability,absorb,mass, texture, model, animation);
    }

}
