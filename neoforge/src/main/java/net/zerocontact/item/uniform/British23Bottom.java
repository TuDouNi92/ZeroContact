package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class British23Bottom extends BaseUniformBottom{
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/uniform/uniform_british23_bottom.png");
    private static final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/uniform/uniform_british23_bottom.geo.json");
    private static final ResourceLocation animation = ResourceLocation.fromNamespaceAndPath(MOD_ID,"");
    public British23Bottom() {
        super(texture, model, animation);
    }
}
