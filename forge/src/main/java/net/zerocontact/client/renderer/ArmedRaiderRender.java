package net.zerocontact.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.models.ArmedRaiderModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ArmedRaiderRender extends GeoEntityRenderer<ArmedRaider> {
    public ArmedRaiderRender(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ArmedRaiderModel());
        this.addRenderLayer(new ArmedRaiderItemLayer(this));
    }
}
