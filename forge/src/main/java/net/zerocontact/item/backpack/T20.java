package net.zerocontact.item.backpack;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class T20 extends BaseBackpack {
    public T20(int containerSize, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(texture, model, animation, containerSize);
    }

    public record BackpackResource(ResourceLocation texture, ResourceLocation model, ResourceLocation animation){};

    public enum Series{
        UMBRA(
                new BackpackResource(
                        new ResourceLocation(MOD_ID,"textures/models/backpack/backpack_t20_umbra.png"),
                        new ResourceLocation(MOD_ID,"geo/backpack/backpack_t20_umbra.geo.json"),
                        new ResourceLocation("")
                )
        ),
        MULTICAM(
                new BackpackResource(
                        new ResourceLocation(MOD_ID,"textures/models/backpack/backpack_t20_multicam.png"),
                        new ResourceLocation(MOD_ID,"geo/backpack/backpack_t20_multicam.geo.json"),
                        new ResourceLocation("")
                )
        )
        ;
        public final BackpackResource backpackResource;
        Series(BackpackResource backpackResource){
            this.backpackResource = backpackResource;
        }
    }

    public static T20 create(Series series, int containerSize){
        return new T20(containerSize,series.backpackResource.texture,series.backpackResource.model,series.backpackResource.animation);
    }
}
