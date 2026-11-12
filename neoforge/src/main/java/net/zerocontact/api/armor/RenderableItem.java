package net.zerocontact.api.armor;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.client.extensions.UniversalArmorExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

import java.util.function.Consumer;

public interface RenderableItem extends GeoItem {
    ResourceLocation getTexture();
    ResourceLocation getModel();
    ResourceLocation getAnimation();

    @Override
    default void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        // GeckoLib invokes this lazily on the physical client only.
        consumer.accept(new UniversalArmorExtensions(getTexture(), getModel(), getAnimation()) {});
    }
}
