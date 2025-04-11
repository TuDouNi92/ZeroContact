package net.zerocontact.renderer;

import net.minecraft.world.entity.Entity;
import net.zerocontact.item.armor.forge.JpcArmorImpl;
import net.zerocontact.models.JpcModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class JpcRender extends GeoArmorRenderer<JpcArmorImpl> {
    public JpcRender() {
        super(new JpcModel());
    }

    @Override
    public void setupAnim(Entity arg, float f, float g, float h, float i, float j) {

    }

}
