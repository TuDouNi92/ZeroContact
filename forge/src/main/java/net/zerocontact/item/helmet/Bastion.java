package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Bastion extends BaseGeoHelmet {
    public Bastion(int absorb, int durability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(absorb, durability, texture, model, animation);
    }

    public record ColorResources(ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
    }

    public enum Color {
        BLACK(
                new ColorResources(
                        new ResourceLocation(MOD_ID, "textures/models/helmet/helmet_bastion_black.png"),
                        new ResourceLocation(MOD_ID, "geo/helmet/helmet_bastion_black.geo.json"),
                        null
                )
        ),
        MULTICAM(
                new ColorResources(
                        new ResourceLocation(MOD_ID, "textures/models/helmet/helmet_bastion_multicam.png"),
                        new ResourceLocation(MOD_ID, "geo/helmet/helmet_bastion_multicam.geo.json"),
                        null
                )
        ),
        GREEN(
                new ColorResources(
                        new ResourceLocation(MOD_ID, "textures/models/helmet/helmet_bastion_green.png"),
                        new ResourceLocation(MOD_ID, "geo/helmet/helmet_bastion_green.geo.json"),
                        null
                )
        );
        private final ColorResources colorResources;
        Color(ColorResources colorResources) {
            this.colorResources = colorResources;
        }
    }
    public static Bastion create(int absorb, int durability,Color color){
        return new Bastion(absorb,durability,color.colorResources.texture, color.colorResources.model, color.colorResources.animation);
    }
}
