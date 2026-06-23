package net.zerocontact.item.backpack;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Backpack extends BaseBackpack {
    public enum Series {
        R6SH118(new ResourceWrapper(
                new ResourceLocation(MOD_ID,"textures/models/backpack/backpack_6sh118_emr.png"),
                new ResourceLocation(MOD_ID,"geo/backpack/backpack_6sh118_emr.geo.json"),
                new ResourceLocation("")
        ));
        private final ResourceWrapper wrapper;

        Series(ResourceWrapper wrapper) {
            this.wrapper = wrapper;
        }
    }

    public record ResourceWrapper(ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
    }

    private Backpack(ResourceLocation texture, ResourceLocation model, ResourceLocation animation, int containerSize) {
        super(texture, model, animation, containerSize);
    }

    public static Backpack create(Series series, int containerSize) {
        return new Backpack(series.wrapper.texture, series.wrapper.model, series.wrapper.animation, containerSize);
    }
}
