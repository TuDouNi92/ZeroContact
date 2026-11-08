package net.zerocontact.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.zerocontact.models.GenerateModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ItemRender<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {
    public ItemRender(ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(new GenerateModel<>(texture, model, animation));
    }
}
