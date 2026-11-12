package net.zerocontact.item.helmet;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.api.armor.HelmetInfoProvider;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class BaseGeoHelmet extends ArmorItem implements HelmetInfoProvider, GeoItem, ICombatArmorItem, IEquipmentTypeTag {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final int defense;
    private final int defaultDurability;
    private final int absorb;
    private final ResourceLocation texture;
    private final ResourceLocation model;
    private final ResourceLocation animation;
    private final float bluntReduction, penetrateReduction;

    public BaseGeoHelmet(int absorb, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation, float bluntReduction, float penetrateReduction) {
        super(ArmorMaterials.IRON, Type.HELMET, new Properties().durability(defaultDurability));
        this.bluntReduction = bluntReduction;
        this.penetrateReduction = penetrateReduction;
        this.defense = 4;
        this.absorb = absorb;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
        this.defaultDurability = defaultDurability;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public ResourceLocation getModel() {
        return model;
    }

    public ResourceLocation getAnimation() {
        return animation;
    }

    @Override
    public @NotNull Type getType() {
        return type;
    }

    @Override
    public int getDefense() {
        return defense;
    }

    @Override
    public int getDefaultDurability() {
        return defaultDurability;
    }

    @Override
    public int getAbsorb() {
        return absorb;
    }

    @Override
    public int generateLoss(float damageAmount, float durabilityLossFactor, int hits) {
        return 1;
    }


    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("protection_class", getAbsorb());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        return super.getDefaultAttributeModifiers(stack);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        ICombatArmorItem.super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.HELMET;
    }

    @Override
    public float generateBlunt() {
        return this.bluntReduction;
    }

    @Override
    public float generatePenetrated() {
        return this.penetrateReduction;
    }
}
