package net.zerocontact.client.renderer;

import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ArmorRender<T extends Item & GeoItem & GeoAnimatable> extends GeoArmorRenderer<T> {
    public ArmorRender(GeoModel<T> model) {
        super(model);
    }
    public static class ItemRender<T extends Item & GeoItem & GeoAnimatable> extends GeoItemRenderer<T> {
        public ItemRender(GeoModel<T> model) {
            super(model);
        }
    }
}
