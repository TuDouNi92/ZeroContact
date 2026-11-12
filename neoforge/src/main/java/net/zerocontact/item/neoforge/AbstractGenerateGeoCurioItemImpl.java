package net.zerocontact.item.neoforge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zerocontact.api.IGeoCurioItem;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class AbstractGenerateGeoCurioItemImpl extends ArmorItem implements IGeoCurioItem {
    protected final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final String id;
    public final ResourceLocation texture, model, animation;

    public AbstractGenerateGeoCurioItemImpl(String id, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation, ArmorItem.Type armorType) {
        super(ArmorMaterials.IRON, armorType, new Properties().durability(defaultDurability));
        this.id = id;
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
    public boolean canEquip(@NotNull ItemStack stack, @NotNull EquipmentSlot armorType, @NotNull LivingEntity entity) {
        return false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        return InteractionResultHolder.fail(player.getItemInHand(usedHand));
    }

    @Override
    public ResourceLocation getTexture() {
        return this.texture;
    }

    @Override
    public ResourceLocation getModel() {
        return this.model;
    }

    @Override
    public ResourceLocation getAnimation() {
        return this.animation;
    }

}
