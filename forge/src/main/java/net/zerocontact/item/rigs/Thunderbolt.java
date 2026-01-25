package net.zerocontact.item.rigs;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Thunderbolt extends BaseRigs{
    private static final ResourceLocation texture = new ResourceLocation(MOD_ID,"textures/models/rigs/rig_thunderbolt_grey.png");
    private static final ResourceLocation model = new ResourceLocation(MOD_ID,"geo/rigs/rig_thunderbolt_grey.geo.json");
    private static final ResourceLocation animation = new ResourceLocation(MOD_ID,"");
    public Thunderbolt(int containerSize) {
        super(texture, model, animation, containerSize);
    }
}
