package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Ratnik extends BaseGeoHelmet {
    public Ratnik(int absorb, int durability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(absorb, durability, texture, model, animation);
    }

    private record ColorResources(ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
    }

    public enum Color {
        EMR(
                new ColorResources(
                        new ResourceLocation(MOD_ID, "textures/models/helmet/helmet_6b47_cover_emr.png"),
                        new ResourceLocation(MOD_ID, "geo/helmet/helmet_6b47_cover_emr.geo.json"),
                        null
                )
        ),
        ARCTIC(
                new ColorResources(
                        new ResourceLocation(MOD_ID, "textures/models/helmet/helmet_6b47_cover_arctic.png"),
                        new ResourceLocation(MOD_ID, "geo/helmet/helmet_6b47_cover_arctic.geo.json"),
                        null
                )
        );
        private final ColorResources colorResources;
        Color(ColorResources colorResources) {
            this.colorResources = colorResources;
        }
    }
    public static Ratnik create(int absorb, int durability,Color color){
        return new Ratnik(absorb,durability,color.colorResources.texture, color.colorResources.model, color.colorResources.animation);
    }
}
