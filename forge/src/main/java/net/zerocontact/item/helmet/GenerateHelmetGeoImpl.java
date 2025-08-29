package net.zerocontact.item.helmet;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.DurabilityLossProvider;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.client.renderer.HelmetRender;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.loader.ItemLoader;
import net.zerocontact.events.ProtectionLevelHelper;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.*;
import java.util.function.Consumer;

public class GenerateHelmetGeoImpl extends BaseArmorGeoImpl implements HelmetInfoProvider, GeoItem, EntityHurtProvider, DurabilityLossProvider {
    private final int defaultDurability;
    private final int absorb;
    public static Set<GenerationRecord> items = new HashSet<>();
    private final float bluntDamage;
    private final float penetrateDamage;
    private final float ricochetDamage;
    private final int durabilityLossProvider;

    private GenerateHelmetGeoImpl(String id, Type type, ResourceLocation texture, ResourceLocation model, ResourceLocation animation, int defense, int absorb, float bluntDamage, float penetrateDamage, float ricochetDamage, int durabilityLossProvider, int defaultDurability) {
        super(type, id, defense, defaultDurability,absorb,0, texture, model, animation);
        this.absorb = absorb;
        this.bluntDamage = bluntDamage;
        this.penetrateDamage = penetrateDamage;
        this.ricochetDamage = ricochetDamage;
        this.durabilityLossProvider = durabilityLossProvider;
        this.defaultDurability = defaultDurability;
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
        if(slot !=EquipmentSlot.HEAD)return super.getAttributeModifiers(slot,stack);
        Multimap<Attribute, AttributeModifier> modifierMultimap = HashMultimap.create();
        stack.getOrCreateTag().putInt("absorb", getAbsorb());
        modifierMultimap.put(Attributes.ARMOR, new AttributeModifier(UUID.nameUUIDFromBytes(("Armor").getBytes()), "CuriosArmorDefense", this.getDefense(), AttributeModifier.Operation.ADDITION));
        return modifierMultimap;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        Component tipsToAdd = Component.translatable(ProtectionLevelHelper.get(getAbsorb()).name()).withStyle(ChatFormatting.AQUA);
        tooltipComponents.add(tipsToAdd);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HelmetRender.HelmetArmorRender<GenerateHelmetGeoImpl> render;

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
                return new HelmetRender.HelmetItemRender<>(new GenerateModel<>(texture, model, animation));
            }
        });
    }

    public static void deserializeItems() {
        ArrayList<ItemGenData> itemGenDataList = ItemLoader.itemGenData;
        if (itemGenDataList.isEmpty()) return;
        for (ItemGenData data0 : itemGenDataList) {
            if (!(data0 instanceof ItemGenData.Armor data)) continue;
            String id = data.id;
            float bluntDamage = data.hurtModifier.bluntMultiplier;
            float penetrateDamage = data.hurtModifier.penetrateMultiplier;
            float ricochetDamage = data.hurtModifier.ricochetMultiplier;
            int defense = data.defense;
            int absorb = data.absorb;
            int durabilityLossProvider = data.durabilityLossModifier;
            int default_durability = data.defaultDurability;
            if (!data.equipmentSlot.equals("HELMET")) continue;
            ResourceLocation texture = new ResourceLocation(ZeroContact.MOD_ID, data.texture);
            ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, data.model);
            ResourceLocation animation = new ResourceLocation(ZeroContact.MOD_ID, data.animation);
            items.add(new GenerationRecord(id,new GenerateHelmetGeoImpl(id, Type.HELMET, texture, model, animation, defense, absorb, bluntDamage, penetrateDamage, ricochetDamage, durabilityLossProvider,default_durability)));
        }
    }

}
