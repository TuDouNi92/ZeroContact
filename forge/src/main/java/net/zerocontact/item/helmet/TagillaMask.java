package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class TagillaMask extends BaseGeoHelmet {
    public TagillaMask(int absorb, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(absorb, defaultDurability, texture, model, animation);
    }

    private record ColorResources(ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
    }

    public enum Color {
        MANHUNT(
                new ColorResources(
                        new ResourceLocation(MOD_ID,"textures/models/helmet/mask_svarka_tagilla_manhunt.png"),
                        new ResourceLocation(MOD_ID,"geo/mask_svarka_tagilla_manhunt.geo.json"),
                        null
                )
        ),
        YBEY(
                new ColorResources(
                        new ResourceLocation(MOD_ID,"textures/models/helmet/mask_svarka_tagilla_ybei.png"),
                        new ResourceLocation(MOD_ID,"geo/mask_svarka_tagilla_ybei.geo.json"),
                        null
                )
        );
        private final ColorResources colorResources;

        Color(ColorResources colorResources) {
            this.colorResources = colorResources;
        }
    }
    public static TagillaMask create(int absorb, int durability,Color color){
        return new TagillaMask(absorb,durability,color.colorResources.texture,color.colorResources.model, color.colorResources.animation);
    }
}
