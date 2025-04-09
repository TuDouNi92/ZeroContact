package net.zerocontact.forge.models;

import net.minecraft.world.entity.Entity;
import net.zerocontact.forge.SapiIVImpl;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class JpcRender extends GeoArmorRenderer<SapiIVImpl> {
    public JpcRender() {
        super(new JpcModel());
    }

    @Override
    public void setupAnim(Entity arg, float f, float g, float h, float i, float j) {

    }

}
