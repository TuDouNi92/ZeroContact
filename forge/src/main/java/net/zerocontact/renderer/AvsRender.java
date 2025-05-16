package net.zerocontact.renderer;

import net.minecraft.world.entity.Entity;
import net.zerocontact.item.armor.forge.AvsArmorImpl;
import net.zerocontact.models.AvsModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class AvsRender extends GeoArmorRenderer<AvsArmorImpl> {
    public AvsRender(){
        super(new AvsModel());
    }
}
