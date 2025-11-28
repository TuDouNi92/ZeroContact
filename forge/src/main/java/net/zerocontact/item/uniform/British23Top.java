package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class British23Top extends BaseUniformTop{
    private static final ResourceLocation texture = new ResourceLocation(MOD_ID,"textures/item/uniform/uniform_british23_top.png");
    private static final ResourceLocation model = new ResourceLocation(MOD_ID,"geo/uniform/uniform_british23_top.geo.json");
    private static final ResourceLocation animation = new ResourceLocation(MOD_ID,"");
    public British23Top() {
        super(texture, model, animation);
    }
}
