package net.zerocontact.item.kit;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.capability.RepairKitCap;
import net.zerocontact.client.renderer.ItemRender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.List;
import java.util.function.Consumer;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ArmorRepairKit extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private static final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/item/kit_armor.png");
    private static final ResourceLocation model = new ResourceLocation(MOD_ID, "geo/kit_armor.geo.json");
    private static final ResourceLocation animation = new ResourceLocation("");

    public ArmorRepairKit() {
        super(new Properties().defaultDurability(256));
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack stack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        return stack.getCapability(CapabilityRegistries.REPAIR_KIT).map(kitManager -> {
            if (action == ClickAction.PRIMARY) {
                kitManager.setInterrupt(true);
            }
            ItemStack slotItemStack = slot.getItem();
            Item slotItem = slotItemStack.getItem();
            int targetSlot = -1;
            if (!(stack.getItem() instanceof ArmorRepairKit)) return false;
            if (action == ClickAction.SECONDARY) {
                if (kitManager.started()) return true;
                if (slotItem instanceof ICombatArmorItem) {
                    boolean shouldInterrupt = kitManager.interrupted();
                    if (shouldInterrupt) return true;
                    if (!kitManager.canRepair(stack, slotItemStack)) return false;
                    kitManager.setRepairItem(serverPlayer, slotItemStack);
                    kitManager.setStarted(true);
                    return true;
                } else {
                    kitManager.stopRepair();
                }
                kitManager.setTargetSlot(targetSlot);
            }
            return false;
        }).orElse(false);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack handStack = player.getItemInHand(usedHand);
        InteractionResultHolder<ItemStack> resultHolder = InteractionResultHolder.consume(handStack);
        player.startUsingItem(usedHand);
        if (player instanceof ServerPlayer) {
            handStack.getCapability(CapabilityRegistries.REPAIR_KIT).ifPresent(kitManager -> kitManager.setStarted(true));
        }
        return resultHolder;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (!(livingEntity instanceof ServerPlayer serverPlayer)) return;
        stack.getCapability(CapabilityRegistries.REPAIR_KIT).ifPresent(kitManager -> {
            int targetSlot = kitManager.getTargetSlot();
            if (targetSlot == -1) {
                kitManager.setTargetSlotFromInv(serverPlayer, stack);
                targetSlot = kitManager.getTargetSlot();
            }
            if (targetSlot == -1) return;
            ItemStack slotItemStack = serverPlayer.getInventory().getItem(targetSlot);
            if (slotItemStack.isEmpty()) return;
            if (!kitManager.started())
                return;

            if (kitManager.interrupted()) {
                kitManager.stopRepair();
                return;
            }

            if (kitManager.shouldRepairAtTick()) {
                kitManager.repair(serverPlayer, stack, slotItemStack);
            }
            if (kitManager.finished()) {
                kitManager.stopRepair();
            }
        });
    }


    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity, int timeCharged) {
        super.releaseUsing(stack, level, livingEntity, timeCharged);

    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ItemRender<ArmorRepairKit> render;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (render == null) {
                    render = new ItemRender<>(texture, model, animation);
                    return render;
                }
                return render;
            }
        });
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        Component helpText = Component.translatable("tootip.zerocontact.kit_armor").withStyle(ChatFormatting.GRAY);
        tooltipComponents.add(helpText);
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    static class KitListener {
        @SubscribeEvent
        public static void onKitUsing(TickEvent.PlayerTickEvent event) {
            if (event.side.isClient()) return;
            Player player = event.player;
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            ItemStack carriedStack = serverPlayer.containerMenu.getCarried();
            carriedStack.getCapability(CapabilityRegistries.REPAIR_KIT).ifPresent(kitManager -> {
                int targetSlot = kitManager.getTargetSlot();
                if (targetSlot == -1) return;
                ItemStack slotItemStack = player.getInventory().getItem(targetSlot);
                if (!kitManager.started())
                    return;

                if (kitManager.interrupted()) {
                    kitManager.stopRepair();
                    return;
                }

                if (kitManager.shouldRepairAtTick()) {
                    kitManager.repair(serverPlayer, carriedStack, slotItemStack);
                }
                if (kitManager.finished()) {
                    kitManager.stopRepair();
                }
            });
        }
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            final RepairKitCap cap = new RepairKitCap();

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
                return CapabilityRegistries.REPAIR_KIT.orEmpty(capability, LazyOptional.of(() -> cap));
            }
        };
    }
}
