package net.zerocontact.client.renderer;

import net.zerocontact.item.helmet.FastMt;
import net.zerocontact.models.FastMtModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HelmetRender extends GeoArmorRenderer<FastMt> {
    public HelmetRender() {
        super(new FastMtModel());
    }

    public static class HelmetItemRender extends GeoItemRenderer<FastMt> {
        public HelmetItemRender() {
            super(new FastMtModel());
        };
    }
}
