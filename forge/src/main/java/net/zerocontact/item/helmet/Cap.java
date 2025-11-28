package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Cap extends BaseGeoHelmet{
    private Cap(int absorb, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(absorb, defaultDurability, texture, model, animation);
    }
    public record ColorResources(ResourceLocation texture, ResourceLocation model, ResourceLocation animation){}
    public enum Color{
        CYAN(
                new ColorResources(
                        new ResourceLocation(MOD_ID,"textures/models/helmet/helmet_cap_cyan.png"),
                        new ResourceLocation(MOD_ID,"geo/helmet_cap_cyan.geo.json"),
                        null
                )
        ),
        BOSS(
                new ColorResources(
                        new ResourceLocation(MOD_ID,"textures/models/helmet/helmet_cap_boss.png"),
                        new ResourceLocation(MOD_ID,"geo/helmet_cap_boss.geo.json"),
                        null
                )
        );
        private final ColorResources resources;
        Color(ColorResources resources){this.resources = resources;}
    }
    public static Cap create(int absorb, int durability, Color color){
        return new Cap(absorb,durability,color.resources.texture, color.resources.model,color.resources.animation);
    }
}
