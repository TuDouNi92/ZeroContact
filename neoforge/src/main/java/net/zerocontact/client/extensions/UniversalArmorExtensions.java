package net.zerocontact.client.extensions;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;


public class UniversalArmorExtensions implements GeoRenderProvider {

    private final ResourceLocation texture;
    private final ResourceLocation model;
    private final ResourceLocation animation;

    private ArmorRender<? extends GeoAnimatable> render;
    private ArmorRender.ItemRender<? extends GeoAnimatable > itemRender;

    public UniversalArmorExtensions(
            ResourceLocation texture,
            ResourceLocation model,
            ResourceLocation animation
    ) {
        this.texture = texture;
        this.model = model;
        this.animation = animation;
    }

    @Override
    public <T extends LivingEntity> @NotNull HumanoidModel<?> getGeoArmorRenderer(
            @Nullable T livingEntity,
            @NotNull ItemStack itemStack,
            @Nullable EquipmentSlot equipmentSlot,
            @Nullable HumanoidModel<T> original
    ) {
        if (render == null) {
            render = new ArmorRender<>(
                    new GenerateModel<>(texture, model, animation)
            );
        }

        return render;
    }

    @Override
    public @NotNull BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
        if (itemRender == null) {
            itemRender = new ArmorRender.ItemRender<>(
                    new GenerateModel<>(texture, model, animation)
            );
        }

        return itemRender;
    }
}
