package net.zerocontact.item.armor.forge;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class R6b43ArmorImpl extends BaseArmorGeoImpl {
    public enum Series {
        FLORA(
                new ResourceLocation(MOD_ID, "textures/models/vest/vest_6b43_flora.png"),
                new ResourceLocation(MOD_ID, "geo/vest/vest_6b43_flora.geo.json"),
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

    R6b43ArmorImpl(Series series, int defense, int defaultDurability, int absorb, float generateReduction, float mass) {
        super(Type.CHESTPLATE, "", defense, defaultDurability, absorb, generateReduction, mass, series.texture, series.model, series.animation);
    }

    public static R6b43ArmorImpl create(Series series, int defense, int defaultDurability, int absorb, float generateReduction, float mass) {
        return new R6b43ArmorImpl(series, defense, defaultDurability, absorb, generateReduction, mass);
    }
}
