package net.zerocontact.item.plate;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.api.armor.PlateInfoProvider;
import net.zerocontact.client.interaction.PlateInteractionManager;
import net.zerocontact.curios.CuriosConstants;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.Optional;

public class BasePlate extends ArmorItem implements PlateInfoProvider, GeoItem, IEquipmentTypeTag {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ResourceLocation texture, model, animation;
    private final float bluntReduction;
    private final float penetrateReduction;
    private final float ricochetReduction;
    private final int defense;
    private final int absorb;
    private final float movementFix;
    private final float durabilityLoss;
    public final RawAnimation installAnim;

    public BasePlate(int durability, int defense, int absorb, float bluntReduction, float penetrateReduction, float ricochetReduction, float movementFix, float durabilityLoss, ResourceLocation texture, ResourceLocation model, ResourceLocation animation) {
        super(ArmorMaterials.IRON, Type.CHESTPLATE, new Properties().durability(durability));
        this.texture = texture;
        this.model = model;
        this.animation = animation.getPath().isBlank()
                ? ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "animations/plate.animation.json")
                : animation;
        this.penetrateReduction = penetrateReduction;
        this.bluntReduction = bluntReduction;
        this.durabilityLoss = durabilityLoss;
        this.defense = defense;
        this.absorb = absorb;
        this.movementFix = movementFix;
        this.ricochetReduction = ricochetReduction;
        this.installAnim = RawAnimation.begin().then("install", Animation.LoopType.PLAY_ONCE);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            ItemStack handStack = player.getMainHandItem();
            if(GeoItem.getId(handStack) == Long.MAX_VALUE){
                GeoItem.getOrAssignId(handStack, (ServerLevel) level);
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack handStack = player.getItemInHand(usedHand);
        if (level.isClientSide && usedHand == InteractionHand.MAIN_HAND) {
            PlateInteractionManager.install(this);
        }
        return InteractionResultHolder.consume(handStack);
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        return super.getDefaultAttributeModifiers(stack);
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar
                .add(new AnimationController<GeoAnimatable>(this, "controller", state -> {
                            if (state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE) == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                                return PlayState.CONTINUE;
                            }
                            return PlayState.STOP;
                        })
                                .triggerableAnim("install", installAnim)
                                .receiveTriggeredAnimations()
                );
    }

    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
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
    public int generateLoss(float damageAmount, float durabilityLossFactor, int hits) {
        return PlateInfoProvider.super.generateLoss(damageAmount, durabilityLoss, hits);
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
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return PlateInfoProvider.super.canEquip(stack, armorType, entity);
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        PlateInfoProvider.super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.PLATE;
    }

    public static void resolveSlot(Player player, TriConsumer<ICuriosItemHandler, Optional<ICurioStacksHandler>, Optional<ICurioStacksHandler>> consumer) {
        CuriosApi.getCuriosInventory(player).ifPresent(i -> {
            Optional<ICurioStacksHandler> frontHandler = i.getStacksHandler(CuriosConstants.FRONT_PLATE);
            Optional<ICurioStacksHandler> backHandler = i.getStacksHandler(CuriosConstants.BACK_PLATE);
            consumer.accept(i, frontHandler, backHandler);
        });
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
