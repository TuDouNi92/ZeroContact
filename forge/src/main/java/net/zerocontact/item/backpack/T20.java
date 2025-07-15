package net.zerocontact.item.backpack;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class T20 extends BaseBackpack {
    private static final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/item/backpack_t20_umbra.png");
    private static final ResourceLocation model = new ResourceLocation(MOD_ID, "geo/backpack_t20_umbra.geo.json");
    private static final ResourceLocation animation = null;

    public T20(int defaultDurability,int containerSize) {
        super("", defaultDurability, texture, model, animation, containerSize);
    }
}
