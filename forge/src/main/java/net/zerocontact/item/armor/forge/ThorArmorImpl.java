package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ThorArmorImpl extends BaseArmorGeoImpl {
    private static final ResourceLocation texture = new ResourceLocation(MOD_ID,"textures/models/vest/vest_thor_black.png");
    private static final ResourceLocation model = new ResourceLocation(MOD_ID,"geo/vest/vest_thor_black.geo.json");
    private static final ResourceLocation animation=null;
    public ThorArmorImpl(int defense, int defaultDurability,int absorb,float mass) {
        super(Type.CHESTPLATE, "", defense, defaultDurability,absorb,mass, texture, model, animation);
    }

    @Override
    public @NotNull ArmorType getArmorType() {
        return ArmorType.PLATE_CARRIER;
    }
}
