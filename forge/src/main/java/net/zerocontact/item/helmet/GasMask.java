package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class GasMask extends BaseGeoHelmet {
    public enum Series {
        PMK2(
                new ResourceLocation(MOD_ID, "textures/models/mask/mask_pmk2_black.png"),
                new ResourceLocation(MOD_ID, "geo/mask/mask_pmk2_black.geo.json"),
                null
        ),
        ZK(
                new ResourceLocation(MOD_ID, "textures/models/mask/mask_zelinsky_kummant_rubber.png"),
                new ResourceLocation(MOD_ID, "geo/mask/mask_zelinsky_kummant_rubber.geo.json"),
                null
        ),
        M50(
                new ResourceLocation(MOD_ID, "textures/models/mask/mask_m50_black.png"),
                new ResourceLocation(MOD_ID, "geo/mask/mask_m50_black.geo.json"),
                null
        ),
        MP5(
                new ResourceLocation(MOD_ID, "textures/models/mask/mask_mp5_black.png"),
                new ResourceLocation(MOD_ID, "geo/mask/mask_mp5_black.geo.json"),
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

    GasMask(Series series, int absorb, int defaultDurability) {
        super(absorb, defaultDurability, series.texture, series.model, series.animation);
    }

    public static GasMask create(Series series, int absorb, int defaultDurability) {
        return new GasMask(series, absorb, defaultDurability);
    }
}
