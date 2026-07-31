package net.zerocontact.api;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.client.renderer.ArmorRender;
import software.bernie.geckolib.animatable.GeoItem;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public interface IGeoCurioItem extends GeoItem, ICurioItem {
     ResourceLocation texture();

     ResourceLocation model();

     ResourceLocation animation();

    default void setArmorRender(ArmorRender<?> render){}
}
