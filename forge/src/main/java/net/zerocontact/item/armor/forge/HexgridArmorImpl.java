package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static net.zerocontact.ZeroContact.MOD_ID;

public class HexgridArmorImpl extends BaseArmorGeoImpl {
    private static final ResourceLocation  texture= new ResourceLocation(MOD_ID,"textures/models/armor/vest_hexgrid_black.png") ;
    private static final ResourceLocation  model= new ResourceLocation(MOD_ID,"geo/vest_hexgrid_black.geo.json") ;
    private static final ResourceLocation  animation= null;
    public HexgridArmorImpl(int defense, int defaultDurability) {
        super(Type.CHESTPLATE, "", defense, defaultDurability, texture, model, animation);
    }

    @Override
    public @NotNull ArmorType getArmorType() {
        return ArmorType.PLATE_CARRIER;
    }
}
