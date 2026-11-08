package net.zerocontact.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public interface IGeoCurioItem extends GeoItem, ICurioItem {
    ResourceLocation texture();

    ResourceLocation model();

    ResourceLocation animation();

}
