package net.zerocontact.item.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.zerocontact.api.armor.RenderableItem;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GeoBlockItem extends BlockItem implements GeoItem, RenderableItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ResourceLocation texture, model, animation;

    public GeoBlockItem(Block block, Properties properties, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(block, properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        this.texture = texture;
        this.model = model;
        this.animation = animation;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public ResourceLocation getModel() {
        return model;
    }

    @Override
    public ResourceLocation getAnimation() {
        return animation;
    }
}
