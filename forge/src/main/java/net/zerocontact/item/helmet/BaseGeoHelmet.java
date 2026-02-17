package net.zerocontact.item.helmet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.client.renderer.HelmetRender;
import net.zerocontact.item.PlateBaseMaterial;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.function.Consumer;

public class BaseGeoHelmet extends ArmorItem implements HelmetInfoProvider, GeoItem, ICombatArmorItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final int defense;
    private final int defaultDurability;
    private final int absorb;
    private final ResourceLocation texture;
    private final ResourceLocation model;
    private final ResourceLocation animation;
    public BaseGeoHelmet(int absorb, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(PlateBaseMaterial.ARMOR_STEEL, Type.HELMET, new Properties().defaultDurability(defaultDurability));
        this.defense = 4;
        this.absorb = absorb;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
        this.defaultDurability = defaultDurability;
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
    public int generateLoss(float damageAmount, float durabilityLossFactor, int hits) {
        return 1;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifierMultimap = HashMultimap.create();
        stack.getOrCreateTag().putInt("protection_class", getAbsorb());
        return modifierMultimap;
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
            private HelmetRender.HelmetArmorRender<FastMt> render;

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
                return new HelmetRender.HelmetItemRender<>(new GenerateModel<>(texture,model,animation));
            }
        });
    }
}
