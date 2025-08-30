package net.zerocontact.item.armband;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.api.ArmorTypeTag;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Armband extends AbstractGenerateGeoCurioItemImpl implements ArmorTypeTag {
    public Armband(ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super("",0,texture, model, animation);
    }
    public record ArmbandResources(ResourceLocation texture, ResourceLocation model, ResourceLocation animation){}

    public enum Series {
        BLACK(
                new ArmbandResources(
                        new ResourceLocation(MOD_ID,"textures/item/armband/armband_black.png"),
                        new ResourceLocation(MOD_ID,"geo/armband/armband_black.geo.json"),
                        new ResourceLocation(MOD_ID,"")
                        )
        ),
        BLUE(
                new ArmbandResources(
                        new ResourceLocation(MOD_ID,"textures/item/armband/armband_blue.png"),
                        new ResourceLocation(MOD_ID,"geo/armband/armband_blue.geo.json"),
                        new ResourceLocation(MOD_ID,"")
                )
        ),
        GREEN(
                new ArmbandResources(
                        new ResourceLocation(MOD_ID,"textures/item/armband/armband_green.png"),
                        new ResourceLocation(MOD_ID,"geo/armband/armband_green.geo.json"),
                        new ResourceLocation(MOD_ID,"")
                )
        ),
        RED(
                new ArmbandResources(
                        new ResourceLocation(MOD_ID,"textures/item/armband/armband_red.png"),
                        new ResourceLocation(MOD_ID,"geo/armband/armband_red.geo.json"),
                        new ResourceLocation(MOD_ID,"")
                )
        ),
        FLORA(
                new ArmbandResources(
                        new ResourceLocation(MOD_ID,"textures/item/armband/armband_vsrf.png"),
                        new ResourceLocation(MOD_ID,"geo/armband/armband_flora.geo.json"),
                        new ResourceLocation(MOD_ID,"")
                )
        ),
        WHITE(
                new ArmbandResources(
                        new ResourceLocation(MOD_ID,"textures/item/armband/armband_white.png"),
                        new ResourceLocation(MOD_ID,"geo/armband/armband_white.geo.json"),
                        new ResourceLocation(MOD_ID,"")
                )
        ),
        YELLOW(
                new ArmbandResources(
                        new ResourceLocation(MOD_ID,"textures/item/armband/armband_yellow.png"),
                        new ResourceLocation(MOD_ID,"geo/armband/armband_yellow.geo.json"),
                        new ResourceLocation(MOD_ID,"")
                )
        );
        public final ArmbandResources resourceLocations;
        Series(ArmbandResources resourceLocations){
            this.resourceLocations = resourceLocations;
        }
    }

    public static Armband create(Series series) {
        return new Armband(series.resourceLocations.texture(),series.resourceLocations.model(),series.resourceLocations.animation());
    }
    @Override
    public @NotNull ArmorType getArmorType() {
        return ArmorType.ARMBAND;
    }
}
