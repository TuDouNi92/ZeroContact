package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class British23Top extends BaseUniformTop{
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/uniform/uniform_british23_top.png");
    private static final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/uniform/uniform_british23_top.geo.json");
    private static final ResourceLocation animation = ResourceLocation.fromNamespaceAndPath(MOD_ID,"");
    public British23Top() {
        super(texture, model, animation);
    }
}
