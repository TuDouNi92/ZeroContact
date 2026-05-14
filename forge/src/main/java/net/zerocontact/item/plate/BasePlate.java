package net.zerocontact.item.plate;

import com.google.common.collect.Multimap;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.api.PlateInfoProvider;
import net.zerocontact.client.renderer.ItemRender;
import net.zerocontact.item.PlateBaseMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

import static net.zerocontact.ZeroContact.MOD_ID;

public class BasePlate extends ArmorItem implements PlateInfoProvider, GeoItem {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ResourceLocation texture, model, animation;
    private final float bluntReduction;
    private final float penetrateReduction;
    private final int defense;
    private final int absorb;
    private final float movementFix;

    private BasePlate(int defense, int absorb, float bluntReduction, float penetrateReduction, float movementFix, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(PlateBaseMaterial.ARMOR_STEEL, Type.CHESTPLATE, new Properties());
        this.texture = texture;
        this.model = model;
        this.animation = animation;
        this.penetrateReduction = penetrateReduction;
        this.bluntReduction = bluntReduction;
        this.defense = defense;
        this.absorb = absorb;
        this.movementFix = movementFix;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<?> render;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (render == null) {
                    render = new ItemRender<>(texture, model, animation);
                }
                return render;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public static BasePlate createGeoPlate(int defense, int absorb, float bluntReduction, float penetrateReduction, float movementFix, String texture, String model, @NotNull String animation) {
        return new BasePlate(defense, absorb, bluntReduction, penetrateReduction, movementFix, new ResourceLocation(MOD_ID, texture), new ResourceLocation(MOD_ID, model), new ResourceLocation(MOD_ID, animation));
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
    public int getAbsorb() {
        return this.absorb;
    }

    @Override
    public int getDefense() {
        return this.defense;
    }

    @Override
    public float getMass() {
        return this.movementFix;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return PlateInfoProvider.super.canEquip(stack, armorType, entity);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        PlateInfoProvider.super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
