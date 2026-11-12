package net.zerocontact.item.helmet;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.*;
import net.zerocontact.api.armor.HelmetInfoProvider;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.api.armor.modular.ModularEquipment;
import net.zerocontact.api.datagen.IAssetManager;
import net.zerocontact.armor.modular.model.MountDefinition;
import net.zerocontact.datagen.model.GenerationRecord;
import net.zerocontact.datagen.model.ItemPOJO;
import net.zerocontact.events.ArmorUnEquippedHelper;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import java.util.*;

public class GenerateHelmetGeoImpl extends BaseArmorGeoImpl implements HelmetInfoProvider, IGeoCurioItem, ICombatArmorItem, IAssetManager.GeneratableItem, ModularEquipment {
    private final int defaultDurability;
    private final int absorb;
    public final Set<GenerationRecord<?>> items = new HashSet<>();
    private final float bluntDamage;
    private final float penetrateDamage;
    private final float ricochetDamage;
    private final float durabilityLossProvider;
    private final ResourceLocation texture, model, animation;
    private final EquipmentType equipmentType;
    private final List<MobEffect> immuneEffects;
    private int effectTick;
    private final List<ItemPOJO.Armor.Attachments> attachments;

    public GenerateHelmetGeoImpl(
            String id,
            Type type,
            ResourceLocation texture,
            ResourceLocation model,
            ResourceLocation animation,
            int defense,
            int absorb,
            float bluntDamage,
            float penetrateDamage,
            float ricochetDamage,
            float durabilityLossProvider,
            int defaultDurability,
            EquipmentType equipmentType,
            List<MobEffect> immuneEffects,
            List<ItemPOJO.Armor.Attachments> attachments) {
        super(type, id, defense, defaultDurability, absorb, bluntDamage, penetrateDamage, 0, texture, model, animation);
        this.absorb = absorb;
        this.bluntDamage = bluntDamage;
        this.penetrateDamage = penetrateDamage;
        this.ricochetDamage = ricochetDamage;
        this.durabilityLossProvider = durabilityLossProvider;
        this.defaultDurability = defaultDurability;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
        this.equipmentType = equipmentType;
        this.immuneEffects = immuneEffects;
        this.attachments = attachments;
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
    public float generateBlunt() {
        return this.bluntDamage;
    }

    @Override
    public float generatePenetrated() {
        return this.penetrateDamage;
    }

    @Override
    public float generateRicochet() {
        return this.ricochetDamage;
    }

    @Override
    public int generateLoss(float damageAmount, float durabilityLossFactor, int hits) {
        return ICombatArmorItem.generateLossDefault(damageAmount, durabilityLossProvider, hits);
    }



    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        ArmorUnEquippedHelper.onArmorUnequipped(slotContext, stack);
        immuneEffectTick(slotContext.entity(), stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if(!(entity instanceof LivingEntity livingEntity))return;
        ItemStack helmetStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        if (stack == helmetStack) {
            immuneEffectTick(livingEntity, helmetStack);
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }


    private void immuneEffectTick(LivingEntity entity, ItemStack stack) {
        if (entity.level().isClientSide) return;
        if (effectTick % 40 == 0) {
            effectTick = 0;
        }
        if (stack.getMaxDamage() - stack.getDamageValue() <= 1) return;
        for (MobEffect immuneEffect : immuneEffects) {
            if (!entity.hasEffect(Holder.direct(immuneEffect))) continue;
            boolean successRemoved = entity.removeEffect(Holder.direct(immuneEffect));
            if (effectTick == 0 && successRemoved) {
                stack.hurtAndBreak(1, entity, EquipmentSlot.HEAD);
            }
        }
        effectTick++;
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return equipmentType;
    }

    @Override
    public Collection<MountDefinition> getMountDefinitions(ItemStack stack) {
        return attachments.stream().map(pojo ->
                new MountDefinition(
                        ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, pojo.mountId),
                        pojo.getMountType(),
                        pojo.mountBone,
                        pojo.getAcceptCategories()
                )
        ).toList();
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
