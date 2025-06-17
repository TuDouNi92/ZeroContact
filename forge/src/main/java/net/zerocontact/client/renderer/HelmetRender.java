package net.zerocontact.client.renderer;

import net.zerocontact.item.forge.Helmet;
import net.zerocontact.models.HelmetModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class HelmetRender extends GeoArmorRenderer<Helmet> {
    public HelmetRender() {
        super(new HelmetModel());
    }
}
