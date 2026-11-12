package net.zerocontact.item.armor.forge;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.api.armor.ICombatArmorItem;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;


import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public abstract class BaseArmorGeoImpl extends ArmorItem implements GeoItem, IEquipmentTypeTag, ICombatArmorItem {
    protected final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected final Type type;
    protected final int defense;
    public final String id;
    protected final ResourceLocation texture, model, animation;
    private final int absorb;
    private final float mass;
    private final float penetrateReduction;
    private final float bluntReduction;
    private final float ricochetReduction;

    public BaseArmorGeoImpl(Type type, String id, int defense, int defaultDurability, int absorb, float bluntReduction, float penetrateReduction, float ricochetReduction, float mass, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(ArmorMaterials.IRON, type, new Properties().durability(defaultDurability));
        this.type = type;
        this.id = id;
        this.defense = defense;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
        this.absorb = absorb;
        this.mass = mass;
        this.penetrateReduction = penetrateReduction;
        this.bluntReduction = bluntReduction;
        this.ricochetReduction = ricochetReduction;
    }

    public BaseArmorGeoImpl(Type type, String id, int defense, int defaultDurability, int absorb, float bluntReduction, float penetrateReduction, float mass, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(ArmorMaterials.IRON, type, new Properties().durability(defaultDurability));
        this.type = type;
        this.id = id;
        this.defense = defense;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
        this.absorb = absorb;
        this.mass = mass;
        this.penetrateReduction = penetrateReduction;
        this.bluntReduction = bluntReduction;
        this.ricochetReduction = 0;
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
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }


    @Override
    public int getAbsorb() {
        return absorb;
    }

    @Override
    public int generateLoss(float damageAmount, float durabilityLossFactor, int hits) {
        return ICombatArmorItem.super.generateLoss(damageAmount, durabilityLossFactor, hits);
    }

    @Override
    public float getMass() {
        return mass;
    }


    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers() {
        return super.getDefaultAttributeModifiers();
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("protection_class", getAbsorb());
        tag.putFloat("movement_fix", getMass());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "armor_attr"),
                                this.getDefense(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.CHEST
                )
                .add(
                        Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "speed_attr"),
                                this.getMass(),
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        ),
                        EquipmentSlotGroup.CHEST
                )

                .build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        ICombatArmorItem.super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }

    @Override
    public float generatePenetrated() {
        return penetrateReduction;
    }

    @Override
    public float generateBlunt() {
        return bluntReduction;
    }

    @Override
    public float generateRicochet() {
        return ricochetReduction;
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

