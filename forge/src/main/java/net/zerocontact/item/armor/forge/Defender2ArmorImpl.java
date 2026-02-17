package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.api.IEquipmentTypeTag;
import org.jetbrains.annotations.NotNull;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Defender2ArmorImpl extends BaseArmorGeoImpl{
    private static final ResourceLocation  texture= new ResourceLocation(MOD_ID,"textures/models/vest/vest_defender_2_olive.png") ;
    private static final ResourceLocation  model= new ResourceLocation(MOD_ID,"geo/vest/vest_defender_2_olive.geo.json") ;
    private static final ResourceLocation  animation= null;
    public Defender2ArmorImpl(int defense, int defaultDurability, int absorb,float mass) {
        super(Type.CHESTPLATE, "", defense, defaultDurability, absorb,mass,texture, model, animation);
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.PLATE_CARRIER;
    }
}

