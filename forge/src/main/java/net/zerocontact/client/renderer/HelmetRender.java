package net.zerocontact.client.renderer;

import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HelmetRender {
    public static class HelmetArmorRender<TItem extends Item & GeoAnimatable & GeoItem> extends GeoArmorRenderer<TItem> {
        public HelmetArmorRender(GeoModel<TItem> model) {
            super(model);
        }
    }
    public static class HelmetItemRender<TItem extends Item & GeoAnimatable & GeoItem> extends GeoItemRenderer<TItem> {
        public HelmetItemRender(GeoModel<TItem> model) {
            super(model);
        }
    }
}
