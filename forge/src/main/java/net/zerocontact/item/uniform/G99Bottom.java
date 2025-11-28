package net.zerocontact.item.uniform;

import net.minecraft.resources.ResourceLocation;

import static net.zerocontact.ZeroContact.MOD_ID;

public class G99Bottom extends BaseUniformBottom{
    private static final ResourceLocation texture = new ResourceLocation(MOD_ID,"textures/item/uniform/uniform_g99_bottom.png");
    private static final ResourceLocation model = new ResourceLocation(MOD_ID,"geo/uniform/uniform_g99_bottom.geo.json");
    private static final ResourceLocation animation = new ResourceLocation(MOD_ID,"");
    public G99Bottom() {
        super(texture, model, animation);
    }
}
