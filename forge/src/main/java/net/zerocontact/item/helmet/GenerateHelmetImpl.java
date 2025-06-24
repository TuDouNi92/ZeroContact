package net.zerocontact.item.helmet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.ZeroContact;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.DurabilityLossProvider;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.client.renderer.HelmetRender;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.loader.ItemLoader;
import net.zerocontact.events.ProtectionLevelHelper;
import net.zerocontact.item.PlateBaseMaterial;
import net.zerocontact.item.armor.forge.GenerateArmorImpl;
import net.zerocontact.models.FastMtModel;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GenerateHelmetImpl extends GenerateArmorImpl implements HelmetInfoProvider, GeoItem, EntityHurtProvider, DurabilityLossProvider {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final Type type;
    private static final ArmorMaterial material = PlateBaseMaterial.ARMOR_STEEL;
    private final int defense;
    private static final int defaultDurability = 24;
    private final int absorb;
    private final ResourceLocation texture;
    private final ResourceLocation model;
    private final ResourceLocation animation;
    public static Set<GenerateArmorImpl> items = new HashSet<>();
    public final String id;
    private final float bluntDamage;
    private final float penetrateDamage;
    private final float ricochetDamage;
    private final int durabilityLossProvider;

    public GenerateHelmetImpl(String id, Type type,ResourceLocation texture, ResourceLocation model, ResourceLocation animation, int defense, int absorb, float bluntDamage, float penetrateDamage, float ricochetDamage, int durabilityLossProvider) {
        super(type, id, defense, defaultDurability, texture, model, animation);
        this.id = id;
        this.type = type;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
        this.defense = defense;
        this.absorb = absorb;
        this.bluntDamage = bluntDamage;
        this.penetrateDamage = penetrateDamage;
        this.ricochetDamage = ricochetDamage;
        this.durabilityLossProvider = durabilityLossProvider;
    }

    @Override
    public @NotNull Type getType() {
        return type;
    }

    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return material;
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
        return this.durabilityLossProvider;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifierMultimap = HashMultimap.create();
        stack.getOrCreateTag().putInt("absorb", getAbsorb());
        return modifierMultimap;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        Component tipsToAdd = Component.translatable(ProtectionLevelHelper.get(getAbsorb()).name()).withStyle(ChatFormatting.AQUA);
        tooltipComponents.add(tipsToAdd);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HelmetRender.HelmetArmorRender<GenerateHelmetImpl> render;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.render == null) {
                    this.render = new HelmetRender.HelmetArmorRender<>(new GenerateModel<>(texture,model,animation));
                }
                this.render.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.render;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new HelmetRender.HelmetItemRender<>(new GenerateModel<>(texture, model, animation));
            }
        });
    }

    public static void regItems() {
        ArrayList<ItemGenData> itemGenDataList = ItemLoader.itemGenData;
        if (itemGenDataList.isEmpty()) return;
        for (ItemGenData data0 : itemGenDataList) {
            if (!(data0 instanceof ItemGenData.Armor data)) continue;
            ZeroContactLogger.LOG.info(new Gson().toJson(data0));
            String id = data.id;
            float bluntDamage = data.hurtModifier.bluntMultiplier;
            float penetrateDamage = data.hurtModifier.penetrateMultiplier;
            float ricochetDamage = data.hurtModifier.ricochetMultiplier;
            int defense = data.defense;
            int absorb = data.absorb;
            int durabilityLossProvider = data.durabilityLossModifier;
            ArmorItem.Type equipmentSlotType = getArmorType(data.equipmentSlot);
            if(equipmentSlotType.equals(Type.CHESTPLATE))continue;
            ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
            ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
            ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
            items.add(new GenerateHelmetImpl(id, equipmentSlotType, texture, model, animation, defense, absorb, bluntDamage, penetrateDamage, ricochetDamage, durabilityLossProvider));
        }
    }

    private static ArmorItem.Type getArmorType(String equipmentSlot) {
        if (equipmentSlot.equals("HEAD")) {
            return Type.HELMET;
        }
        return Type.CHESTPLATE;
    }
}
