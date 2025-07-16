package net.zerocontact.item.dogtag;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.function.Consumer;

import static net.zerocontact.ZeroContact.MOD_ID;

public class DogTag extends BaseDogTag{
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final ResourceLocation texture = new ResourceLocation(MOD_ID,"textures/item/dogtag.png");
    private final ResourceLocation model = new ResourceLocation(MOD_ID, "geo/dogtag.geo.json");
    public DogTag(Properties properties) {
        super(properties);
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ArmorRender.ItemRender<DogTag> dogTagRender;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if(dogTagRender==null){
                    dogTagRender = new ArmorRender.ItemRender<>(new GenerateModel<>(texture,model,null));
                }
                return dogTagRender;
            }
        });
    }
}
