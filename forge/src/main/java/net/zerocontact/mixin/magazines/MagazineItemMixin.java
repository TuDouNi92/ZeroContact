package net.zerocontact.mixin.magazines;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.raiiiden.taczmagazines.item.MagazineAmmoSource;
import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.magazine.MagazineFamilySystem;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.compat.MagazinesCompat;
import net.zerocontact.compat.MagazinesCompatHandler;
import net.zerocontact.item.ammo.GenerateAmmo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.raiiiden.taczmagazines.item.MagazineItem.getMagazineFamilyId;

@Mixin(value = MagazineItem.class)
public abstract class MagazineItemMixin {
    @Shadow(remap = false)
    public abstract int getAmmoCount(ItemStack magazine);

    @WrapMethod(method = "setAmmoCount", remap = false)
    private void zeroContact$count(ItemStack magazine, int count, Operation<Void> original) {
        int previous = getAmmoCount(magazine);
        original.call(magazine, count);
        MagazinesCompatHandler.get().getCompat().ifPresent(compat ->
                compat.updateMagazineCount(magazine, previous, getAmmoCount(magazine)));
    }

    @WrapMethod(method = "overrideStackedOnOther")
    private boolean zeroContact$loadOnOther(ItemStack stack, Slot slot, ClickAction action,
                                            Player player, Operation<Boolean> original) {
        MagazinesCompat compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        if (!slot.getItem().isEmpty() && !compat.canLoadAmmo(stack, slot.getItem())) return false;
        try (var ignored = compat.beginTransfer(stack, slot.getItem(), player)) {
            return original.call(stack, slot, action, player);
        }
    }

    @WrapMethod(method = "overrideOtherStackedOnMe")
    private boolean zeroContact$loadOnMe(ItemStack magazine, ItemStack heldStack, Slot slot, ClickAction action,
                                         Player player, SlotAccess heldAccess, Operation<Boolean> original) {
        MagazinesCompat compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        if (!heldStack.isEmpty() && !compat.canLoadAmmo(magazine, heldStack)) return false;
        try (var ignored = compat.beginTransfer(magazine, heldStack, player)) {
            return original.call(magazine, heldStack, slot, action, player, heldAccess);
        }
    }

    @WrapMethod(method = {"transferOneBulletOut", "unloadAll"}, remap = false)
    private boolean zeroContact$unload(ItemStack magazine, Player player, Operation<Boolean> original) {
        MagazinesCompat compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        try (var ignored = compat.beginTransfer(magazine, ItemStack.EMPTY, player)) {
            return original.call(magazine, player);
        }
    }

    @WrapMethod(method = "unloadAllCreative", remap = false)
    private boolean zeroContact$unloadCreative(ItemStack magazine, Player player, SlotAccess heldAccess,
                                               Operation<Boolean> original) {
        MagazinesCompat compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        try (var ignored = compat.beginTransfer(magazine, ItemStack.EMPTY, player)) {
            return original.call(magazine, player, heldAccess);
        }
    }

    @WrapMethod(method = "use")
    private InteractionResultHolder<ItemStack> zeroContact$use(Level level, Player player,
                                                               InteractionHand hand,
                                                               Operation<InteractionResultHolder<ItemStack>> original) {
        MagazinesCompat compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        try (var ignored = compat.beginTransfer(player.getItemInHand(hand), ItemStack.EMPTY, player)) {
            return original.call(level, player, hand);
        }
    }

    @WrapOperation(method = {"transferOneBulletOut", "unloadAll", "unloadAllCreative"}, remap = false,
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/builder/AmmoItemBuilder;build()Lnet/minecraft/world/item/ItemStack;", remap = false))
    private ItemStack zeroContact$returnRound(AmmoItemBuilder builder, Operation<ItemStack> original) {
        ItemStack result = original.call(builder);
        return MagazinesCompatHandler.get().getCompat().map(compat -> compat.returnedRounds(result)).orElse(result);
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE",
            target = "Lcom/tacz/guns/api/item/builder/AmmoItemBuilder;build()Lnet/minecraft/world/item/ItemStack;", remap = false))
    private ItemStack zeroContact$returnUsedRound(AmmoItemBuilder builder, Operation<ItemStack> original) {
        return zeroContact$returnRound(builder, original);
    }

    @ModifyVariable(method = "overrideOtherStackedOnMe", at = @At("STORE"), name = "heldAmmoId", remap = false)
    private ResourceLocation zeroContact$creativeAmmoId(ResourceLocation heldAmmoId, ItemStack magazine, ItemStack heldStack,
                                                        Slot slot, ClickAction action, Player player, SlotAccess heldAccess) {
        if (!player.getAbilities().instabuild) return heldAmmoId;
        ResourceLocation familyAmmo = MagazineFamilySystem.getAmmoTypeForFamily(getMagazineFamilyId(magazine));
        ResourceLocation actual = MagazineAmmoSource.compatibleAmmoId(heldStack, familyAmmo);
        return actual != null && !actual.equals(DefaultAssets.EMPTY_AMMO_ID) ? actual : heldAmmoId;
    }

    @Inject(method = "appendHoverText",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    ordinal = 0,
                    shift = At.Shift.AFTER))
    public void appendCartridgeText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        AmmoInjector.AmmoContext context = AmmoInjector.read(stack);
        if (context.isEmpty()) return;
        String variantId = context.caliber().variant();
        Item ammoItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(variantId));
        if (ammoItem == null) return;
        MutableComponent ammoLabel = Component.translatable("tooltip.zerocontact.gun.ammoVariant").withStyle(ChatFormatting.GOLD).append(":");
        Component ammoDescription = Component.literal("\uD83E\uDC35 ").append(Component.translatable(ammoItem.getDefaultInstance().getDescriptionId())).withStyle(ChatFormatting.YELLOW);
        if (!(ammoItem instanceof GenerateAmmo))
            ammoDescription = Component.translatable("hud.zerocontact.ammo.default").withStyle(ChatFormatting.YELLOW);
        ammoLabel.append(ammoDescription);
        tooltip.add(ammoLabel);
    }
}
