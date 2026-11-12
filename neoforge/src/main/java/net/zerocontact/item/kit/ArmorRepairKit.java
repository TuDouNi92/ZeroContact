package net.zerocontact.item.kit;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.api.armor.RenderableItem;
import net.zerocontact.item.kit.api.IRepairKit;
import net.zerocontact.item.kit.event.RepairController;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ArmorRepairKit extends Item implements GeoItem, IRepairKit, RenderableItem {
    private final int repairInterval = 20;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/item/kit_armor.png");
    private static final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(MOD_ID, "geo/kit_armor.geo.json");
    private static final ResourceLocation animation = ResourceLocation.parse("");


    public ArmorRepairKit() {
        super(new Properties().durability(256));
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack stack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player) {
        RepairController controller = RepairController.get();
        ItemStack slotItemStack = slot.getItem();
        Item slotItem = slotItemStack.getItem();
        if (action == ClickAction.PRIMARY) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (controller.started(serverPlayer)) {
                    controller.stop(serverPlayer);
                }
            }
            return false;
        }
        if (action == ClickAction.SECONDARY) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (controller.started(serverPlayer)) {
                    controller.stop(serverPlayer);
                } else {
                    if (slotItem instanceof ICombatArmorItem) {
                        if (!this.canRepair(stack, slotItemStack)) return false;
                        controller.start(serverPlayer, slot.index, player.containerMenu);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack handStack = player.getItemInHand(usedHand);
        InteractionResultHolder<ItemStack> resultHolder = InteractionResultHolder.consume(handStack);
        player.startUsingItem(usedHand);
        if (player instanceof ServerPlayer serverPlayer) {
            RepairController.get().start(serverPlayer, -1, null);
        }
        return resultHolder;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (!(livingEntity instanceof ServerPlayer serverPlayer)) return;
        RepairController controller = RepairController.get();
        RepairSession session = controller.getSession(serverPlayer);
        if (session == null) return;
        session.tick();
        int targetSlot = session.getTargetSlot();

        ItemStack slotItemStack;
        if (targetSlot == -1) {
            slotItemStack = serverPlayer.getInventory().items
                    .stream()
                    .filter(
                            stack1 ->
                                    stack1.getItem() instanceof ICombatArmorItem
                                            && this.canRepair(stack, stack1))
                    .findFirst().orElse(ItemStack.EMPTY);
        } else {
            slotItemStack = serverPlayer.getInventory().getItem(targetSlot);
        }

        if (slotItemStack.isEmpty()) {
            controller.stop(serverPlayer);
            return;
        }

        if (session.shouldRepairAtTick(repairInterval)) {
            boolean success = this.repair(serverPlayer, stack, slotItemStack);
            if (!success) controller.stop(serverPlayer);
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity, int timeCharged) {
        super.releaseUsing(stack, level, livingEntity, timeCharged);
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            RepairController controller = RepairController.get();
            controller.stop(serverPlayer);
        }
    }


    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return Integer.MAX_VALUE;
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
    public void appendHoverText(
            @NotNull ItemStack stack, @NotNull TooltipContext context,
            List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        Component helpText = Component.translatable("tootip.zerocontact.kit_armor").withStyle(ChatFormatting.GRAY);
        tooltipComponents.add(helpText);
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public int gerRepairInterval(ItemStack kit) {
        return repairInterval;
    }

    @Override
    public boolean canRepair(@NotNull ItemStack kit, ItemStack slotItemStack) {
        int kitRemain = kit.getMaxDamage() - kit.getDamageValue();
        int armorNeeded = slotItemStack.getDamageValue();
        return armorNeeded > 0 && kitRemain >= armorNeeded;
    }

    @Override
    public boolean repair(ServerPlayer player, ItemStack kitStack, ItemStack slotItemStack) {
        if (!(kitStack.getItem() instanceof IRepairKit) || !(slotItemStack.getItem() instanceof ICombatArmorItem))
            return false;
        if (!canRepair(kitStack, slotItemStack)) return false;
        kitStack.hurtAndBreak(1, (ServerLevel) player.level(), player, __ -> player.playNotifySound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, SoundSource.PLAYERS, 1.0f, 1.0f));
        slotItemStack.setDamageValue(slotItemStack.getDamageValue() - 1);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS);
        return true;
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

    @EventBusSubscriber(modid = ZeroContact.MOD_ID)
    static class KitListener {

        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            Player player = event.getEntity();
            if (player.level().isClientSide) return;
            if (RepairController.get().started((ServerPlayer) player))
                RepairController.get().stop((ServerPlayer) player);
        }

        @SubscribeEvent
        public static void onKitUsingPost(PlayerTickEvent.Post event) {
            onKitUsingByMenu(event.getEntity());
        }

        private static void onKitUsingByMenu(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            ItemStack carriedStack = serverPlayer.containerMenu.getCarried();
            RepairController controller = RepairController.get();
            RepairSession session = controller.getSession(serverPlayer);
            if (session == null) return;
            int targetSlot = session.getTargetSlot();
            AbstractContainerMenu menu = session.getMenu();
            if (menu == null) return; //Skip Hand use
            if (menu != player.containerMenu
                    || (targetSlot < 0 || player.containerMenu.slots.size() <= targetSlot)
            ) {
                controller.stop(serverPlayer);
                return;
            }
            ItemStack currentSlotStack = player.containerMenu.slots.get(targetSlot).getItem();

            if (session.getSnapStack() != currentSlotStack) {
                controller.stop(serverPlayer);
                return;
            }

            ItemStack slotItemStack = menu.slots.stream()
                    .filter(slot -> slot.index == targetSlot)
                    .findFirst()
                    .map(Slot::getItem)
                    .orElse(ItemStack.EMPTY);

            if (slotItemStack.isEmpty()) controller.stop(serverPlayer);

            if (!(carriedStack.getItem() instanceof IRepairKit repairKit)) {
                controller.stop(serverPlayer);
                return;
            }

            session.tick();
            if (session.shouldRepairAtTick(repairKit.gerRepairInterval(carriedStack))) {
                boolean success = repairKit.repair(serverPlayer, carriedStack, slotItemStack);
                if (!success) controller.stop(serverPlayer);
            }
        }
    }
}
