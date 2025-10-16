package net.zerocontact.item.plate;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.client.renderer.ItemRender;
import net.zerocontact.item.PlateBaseMaterial;
import net.zerocontact.item.forge.SapiIVImpl;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

import static net.zerocontact.ZeroContact.MOD_ID;

public class BasePlate extends SapiIVImpl implements GeoItem {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ResourceLocation texture, model, animation;

    private BasePlate(int defense, int absorb, float movementFix, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(PlateBaseMaterial.ARMOR_STEEL, Type.CHESTPLATE, new Properties(), defense, absorb, movementFix);
        this.texture = texture;
        this.model = model;
        this.animation = animation;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<?> render;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (render == null) {
                    render = new ItemRender<>(texture, model, animation);
                }
                return render;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public static BasePlate createGeoPlate(int defense, int absorb, float movementFix, String texture, String model, @NotNull String animation) {
        return new BasePlate(defense, absorb, movementFix, new ResourceLocation(MOD_ID, texture), new ResourceLocation(MOD_ID, model), new ResourceLocation(MOD_ID, animation));
    }
}
