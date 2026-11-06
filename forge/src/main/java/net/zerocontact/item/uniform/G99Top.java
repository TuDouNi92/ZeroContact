package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class G99Top extends BaseUniformTop{
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MOD_ID,"textures/models/uniform/uniform_g99_top.png");
    private static final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(MOD_ID,"geo/uniform/uniform_g99_top.geo.json");
    private static final ResourceLocation animation = ResourceLocation.fromNamespaceAndPath(MOD_ID,"");
    public G99Top() {
        super(texture, model, animation);
    }
}
