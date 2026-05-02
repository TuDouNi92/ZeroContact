package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.api.IEquipmentTypeTag;
import org.jetbrains.annotations.NotNull;

import static net.zerocontact.ZeroContact.MOD_ID;

public class AvsArmorImpl extends BaseArmorGeoImpl {
    private static final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/models/armorrig/armorrig_avs_tagilla_desert.png");
    private static final ResourceLocation model = new ResourceLocation(MOD_ID, "geo/vest/vest_avs_tagilla_desert.geo.json");
    private static final ResourceLocation animation = null;

    public AvsArmorImpl(int defense, int defaultDurability, int absorb, float generateReduction, float mass) {
        super(Type.CHESTPLATE, "", defense, defaultDurability, absorb, generateReduction, mass, texture, model, animation);
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.PLATE_CARRIER;
    }
}
