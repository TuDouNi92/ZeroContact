package net.zerocontact.item.armor.forge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.api.ArmorTypeTag;
import net.zerocontact.api.DurabilityLossProvider;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.ProtectionInfoProvider;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.events.ProtectionLevelHelper;
import net.zerocontact.item.PlateBaseMaterial;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class BaseArmorGeoImpl extends ArmorItem implements GeoItem, ArmorTypeTag, ProtectionInfoProvider, DurabilityLossProvider, EntityHurtProvider {
    protected final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    protected final Type type;
    protected final int defense;
    public final String id;
    protected final ResourceLocation texture, model, animation;
    private final int absorb;
    private final float mass;

    public BaseArmorGeoImpl(Type type, String id, int defense, int defaultDurability, int absorb, float mass, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(PlateBaseMaterial.ARMOR_STEEL, type, new Properties().defaultDurability(defaultDurability));
        this.type = type;
        this.id = id;
        this.defense = defense;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
        this.absorb = absorb;
        this.mass = mass;
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
                if (render == null) {
                    this.render = new ArmorRender<>(new GenerateModel<>(texture, model, animation));
                }
                render.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return render;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (itemRender == null) {
                    this.itemRender = new ArmorRender.ItemRender<>(new GenerateModel<>(texture, model, animation));
                }
                return itemRender;
            }
        });
    }

    @Override
    public int getAbsorb() {
        return absorb;
    }

    @Override
    public int generateLoss(float damageAmount, float durabilityLossFactor, int hits) {
        return DurabilityLossProvider.super.generateLoss(damageAmount, durabilityLossFactor, hits);
    }

    @Override
    public float getMass() {
        return mass;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        Component tipsToAdd = Component.translatable(ProtectionLevelHelper.get(getAbsorb()).name()).withStyle(ChatFormatting.AQUA);
        tooltipComponents.add(tipsToAdd);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if(slot !=EquipmentSlot.CHEST)return super.getAttributeModifiers(slot,stack);
        Multimap<Attribute, AttributeModifier> modifierMultimap = HashMultimap.create();
        stack.getOrCreateTag().putInt("absorb", getAbsorb());
        stack.getOrCreateTag().putFloat("movement_fix",getMass());
        modifierMultimap.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.nameUUIDFromBytes(("MoveSpeed").getBytes()), "MoveSpeed", getMass(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifierMultimap;
    }
}

