package net.zerocontact.item.helmet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.api.*;
import net.zerocontact.client.renderer.HelmetRender;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.events.ArmorUnEquippedHelper;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import java.util.*;
import java.util.function.Consumer;

public class GenerateHelmetGeoImpl extends BaseArmorGeoImpl implements HelmetInfoProvider, IGeoCurioItem, ICombatArmorItem, IAssetManager.GeneratableItem {
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

    public GenerateHelmetGeoImpl(String id, Type type, ResourceLocation texture, ResourceLocation model, ResourceLocation animation, int defense, int absorb, float bluntDamage, float penetrateDamage, float ricochetDamage, float durabilityLossProvider, int defaultDurability, EquipmentType equipmentType, List<MobEffect> immuneEffects) {
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
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.HEAD) return super.getAttributeModifiers(slot, stack);
        Multimap<Attribute, AttributeModifier> modifierMultimap = HashMultimap.create();
        stack.getOrCreateTag().putInt("protection_class", getAbsorb());
        modifierMultimap.put(Attributes.ARMOR, new AttributeModifier(UUID.nameUUIDFromBytes(("Armor").getBytes()), "CuriosArmorDefense", this.getDefense(), AttributeModifier.Operation.ADDITION));
        return modifierMultimap;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HelmetRender.HelmetArmorRender<GenerateHelmetGeoImpl> render;
            private HelmetRender.HelmetItemRender<GenerateHelmetGeoImpl> itemRender;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.render == null) {
                    this.render = new HelmetRender.HelmetArmorRender<>(new GenerateModel<>(texture, model, animation));
                }
                this.render.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.render;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.itemRender == null) {
                    this.itemRender = new HelmetRender.HelmetItemRender<>(new GenerateModel<>(texture, model, animation));
                }
                return this.itemRender;
            }
        });
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        ArmorUnEquippedHelper.onArmorUnequipped(slotContext, stack);
        immuneEffectTick(slotContext.entity(), stack);
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
        ItemStack helmetStack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (stack == helmetStack) {
            immuneEffectTick(player, helmetStack);
        }
    }

    private void immuneEffectTick(LivingEntity entity, ItemStack stack) {
        if (entity.level().isClientSide) return;
        if (effectTick % 40 == 0) {
            effectTick = 0;
        }
        if (stack.getMaxDamage() - stack.getDamageValue() <= 1) return;
        for (MobEffect immuneEffect : immuneEffects) {
            if (!entity.hasEffect(immuneEffect)) continue;
            boolean successRemoved = entity.removeEffect(immuneEffect);
            if (effectTick == 0 && successRemoved) {
                stack.hurtAndBreak(1, entity, lv -> {
                });
            }
        }
        effectTick++;
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return equipmentType;
    }

    @Override
    public ResourceLocation texture() {
        return this.texture;
    }

    @Override
    public ResourceLocation model() {
        return this.model;
    }

    @Override
    public ResourceLocation animation() {
        return this.animation;
    }
}
