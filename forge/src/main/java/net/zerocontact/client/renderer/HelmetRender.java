package net.zerocontact.client.renderer;

import net.zerocontact.item.helmet.Helmet;
import net.zerocontact.models.HelmetModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HelmetRender extends GeoArmorRenderer<Helmet> {
    public HelmetRender() {
        super(new HelmetModel());
    }

    public static class HelmetItemRender extends GeoItemRenderer<Helmet> {
        public HelmetItemRender() {
            super(new HelmetModel());
        };
    }
}
