package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class UntarArmorImpl extends BaseArmorGeoImpl{
    private static final ResourceLocation  texture= new ResourceLocation(MOD_ID,"textures/models/armor/vest_untar_blue.png") ;
    private static final ResourceLocation  model= new ResourceLocation(MOD_ID,"geo/vest_untar_blue.geo.json") ;
    private static final ResourceLocation  animation= null;
    public UntarArmorImpl(int defense, int defaultDurability,int absorb,float mass) {
        super(Type.CHESTPLATE, "", defense, defaultDurability,absorb,mass, texture, model, animation);
    }
}
