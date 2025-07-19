package net.zerocontact.item.forge;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.item.PlateBaseMaterial;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.function.Consumer;

public abstract class AbstractGenerateGeoCurioItemImpl extends ArmorItem implements ICurioItem, GeoItem {
    protected final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    public final String id;
    public final ResourceLocation texture, model, animation;
    public ArmorRender<AbstractGenerateGeoCurioItemImpl> render;

    public AbstractGenerateGeoCurioItemImpl(String id, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(PlateBaseMaterial.ARMOR_STEEL, ArmorItem.Type.CHESTPLATE, new Properties().defaultDurability(defaultDurability));
        this.id = id;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
        this.render = null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        return InteractionResultHolder.fail(player.getItemInHand(usedHand));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (texture == null || model == null) return render;
                if (render == null) {
                    render = new ArmorRender<>(new GenerateModel<>(texture, model, animation));
                }
                render.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return render;
            }

            private ArmorRender.ItemRender<BaseArmorGeoImpl> itemRender;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (itemRender == null) {
                    this.itemRender = new ArmorRender.ItemRender<>(new GenerateModel<>(texture, model, animation));
                }
                return itemRender;
            }
        });
    }
}
