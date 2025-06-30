package net.zerocontact.item.armor.forge;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.api.ArmorTypeTag;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.item.PlateBaseMaterial;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.function.Consumer;

public abstract class BaseArmorGeoImpl extends ArmorItem implements GeoItem, ArmorTypeTag {
    protected final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    protected final Type type;
    protected final int defense;
    public final String id;
    protected final ResourceLocation texture, model, animation;

    public BaseArmorGeoImpl(Type type, String id, int defense, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(PlateBaseMaterial.ARMOR_STEEL, type, new Properties().defaultDurability(defaultDurability));
        this.type = type;
        this.id = id;
        this.defense = defense;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
    }

    @Override
    public @NotNull Type getType() {
        return type;
    }

    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return PlateBaseMaterial.ARMOR_STEEL;
    }

    @Override
    public int getDefense() {
        return defense;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }
    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ArmorRender<BaseArmorGeoImpl> render;
            private ArmorRender.ItemRender<BaseArmorGeoImpl> itemRender;
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if(render==null){
                    this.render = new ArmorRender<>(new GenerateModel<>(texture, model, animation));
                }
                render.prepForRender(livingEntity,itemStack,equipmentSlot,original);
                return render;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if(itemRender ==null){
                    this.itemRender = new ArmorRender.ItemRender<>(new GenerateModel<>(texture,model,animation));
                }
                return itemRender;
            }
        });
    }
}

