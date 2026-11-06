package net.zerocontact.item.backpack;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Vkbo extends BaseBackpack{
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/backpack/backpack_vkbo_olive.png");
    private static final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/backpack/backpack_vkbo_olive.geo.json");
    private static final ResourceLocation animation = ResourceLocation.fromNamespaceAndPath(MOD_ID,"");
    public Vkbo(int containerSize) {
        super(texture, model, animation, containerSize);
    }
}
