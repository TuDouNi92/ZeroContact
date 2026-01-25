package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class British23Bottom extends BaseUniformBottom{
    private static final ResourceLocation texture = new ResourceLocation(MOD_ID,"textures/models/uniform/uniform_british23_bottom.png");
    private static final ResourceLocation model = new ResourceLocation(MOD_ID,"geo/uniform/uniform_british23_bottom.geo.json");
    private static final ResourceLocation animation = new ResourceLocation(MOD_ID,"");
    public British23Bottom() {
        super(texture, model, animation);
    }
}
