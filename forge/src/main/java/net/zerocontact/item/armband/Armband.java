package net.zerocontact.item.armband;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import org.jetbrains.annotations.NotNull;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Armband extends AbstractGenerateGeoCurioItemImpl implements IEquipmentTypeTag {
    public Armband(ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super("", 0, texture, model, animation, ArmorItem.Type.CHESTPLATE);
    }
    public record ArmbandResources(ResourceLocation texture, ResourceLocation model, ResourceLocation animation){}

    public enum Series {
        BLACK(
                new ArmbandResources(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/armband/armband_black.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/armband/armband_black.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"")
                        )
        ),
        BLUE(
                new ArmbandResources(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/armband/armband_blue.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/armband/armband_blue.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"")
                )
        ),
        GREEN(
                new ArmbandResources(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/armband/armband_green.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/armband/armband_green.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"")
                )
        ),
        RED(
                new ArmbandResources(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/armband/armband_red.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/armband/armband_red.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"")
                )
        ),
        FLORA(
                new ArmbandResources(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/armband/armband_vsrf.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/armband/armband_flora.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"")
                )
        ),
        WHITE(
                new ArmbandResources(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/armband/armband_white.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/armband/armband_white.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"")
                )
        ),
        YELLOW(
                new ArmbandResources(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/armband/armband_yellow.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/armband/armband_yellow.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID,"")
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
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.ARMBAND;
    }
}
