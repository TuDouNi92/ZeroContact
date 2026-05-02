package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.api.IEquipmentTypeTag;
import org.jetbrains.annotations.NotNull;

import static net.zerocontact.ZeroContact.MOD_ID;

public class JpcArmorImpl extends BaseArmorGeoImpl {
    public enum Series {
        V1(
                new ResourceLocation(MOD_ID, "textures/models/armorrig/armorrig_jpc_1v_tan.png"),
                new ResourceLocation(MOD_ID, "geo/armorrig/armorrig_jpc_1v_tan.geo.json"),
                null
        ),
        V2(
                new ResourceLocation(MOD_ID, "textures/models/armorrig/armorrig_jpc_2v_tan.png"),
                new ResourceLocation(MOD_ID, "geo/armorrig/armorrig_jpc_2v_tan.geo.json"),
                null
        ),
        V2SC(
                new ResourceLocation(MOD_ID, "textures/models/armorrig/armorrig_jpc_2v_sc_tan.png"),
                new ResourceLocation(MOD_ID, "geo/armorrig/armorrig_jpc_2v_sc_tan.geo.json"),
                null
        );
        private final ResourceLocation texture;
        private final ResourceLocation model;
        private final ResourceLocation animation;

        Series(ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
            this.texture = texture;
            this.model = model;
            this.animation = animation;
        }
    }

    JpcArmorImpl(int defense, int defaultDurability, int absorb, float mass, Series series) {
        super(Type.CHESTPLATE, "", defense, defaultDurability, absorb, mass, series.texture, series.model, series.animation);
    }

    public static JpcArmorImpl create(Series series, int defense, int defaultDurability, int absorb, float mass) {
        return new JpcArmorImpl(defense, defaultDurability, absorb, mass, series);
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.PLATE_CARRIER;
    }
}
