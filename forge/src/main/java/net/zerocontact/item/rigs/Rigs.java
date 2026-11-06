package net.zerocontact.item.rigs;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Rigs extends BaseRigs {
    public record Resource(ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
    }

    public enum Series {
        THUNDERBOLT(
                new Resource(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/models/rigs/rig_thunderbolt_grey.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "geo/rigs/rig_thunderbolt_grey.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "")
                )
        ),
        ALICE(
                new Resource(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/models/rigs/rig_alice_olive.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "geo/rigs/rig_alice_olive.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "")
                )
        ),
        CR498(
                new Resource(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/models/rigs/rig_cr498_desert.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "geo/rigs/rig_cr498_desert.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "")
                )
        ),
        FAST_TAC_VEST(
                new Resource(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/models/rigs/rig_fast_tac_vest_olive.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "geo/rigs/rig_fast_tac_vest_olive.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "")
                )
        ),
        SOP_MR(
                new Resource(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/models/rigs/rig_sop_mr_desert.png"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "geo/rigs/rig_sop_mr_desert.geo.json"),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "")
                )
        );

        Series(Resource resource) {
            this.resource = resource;
        }

        public final Resource resource;
    }

    private Rigs(ResourceLocation texture, ResourceLocation model, ResourceLocation animation, int containerSize) {
        super(texture, model, animation, containerSize);
    }

    public static Rigs create(Series series, int containerSize) {
        return new Rigs(series.resource.texture, series.resource.model, series.resource.animation, containerSize);
    }
}
