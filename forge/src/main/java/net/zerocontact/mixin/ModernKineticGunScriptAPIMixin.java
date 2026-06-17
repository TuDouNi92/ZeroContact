package net.zerocontact.mixin;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.events.AmmoInjector;
import net.zerocontact.events.EventUtil;
import net.zerocontact.network.ServerAmmoSelector;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Mixin(ModernKineticGunScriptAPI.class)
public abstract class ModernKineticGunScriptAPIMixin {
    @Shadow(remap = false)
    private LivingEntity shooter;
    @Shadow(remap = false)
    private AbstractGunItem abstractGunItem;
    @Shadow(remap = false)
    private ItemStack itemStack;

    @Inject(method = "consumeAmmoFromPlayer", at = @At("HEAD"), remap = false, cancellable = true)
    public void zeroContact$consumeAmmoFromPlayerRigs(int neededAmount, CallbackInfoReturnable<Integer> cir) {
        ItemStack rigs = EventUtil.getCuriosStackFirst(shooter, "rigs");
        if (shooter instanceof ServerPlayer player) {
            if (!player.isCreative()) {
                if (rigs.isEmpty()) {
                    shooter.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(itemHandler -> zeroContact$extractSyncTag(neededAmount, cir, itemHandler, null));
                } else {
                    rigs.getCapability(ForgeCapabilities.ITEM_HANDLER, null).map(itemHandler -> zeroContact$extractSyncTag(neededAmount, cir, itemHandler, rigs));
                }
            } else {
                zeroContact$extractSyncTag(neededAmount, cir, zeroContact$getCreativeHandler(), null);
            }
            cir.cancel();
        }
    }

    @Inject(method = "isReloadingNeedConsumeAmmo", at = @At("RETURN"), remap = false, cancellable = true)
    private void zeroContact$creativeConsumeFakeAmmo(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }


    @Unique
    private IItemHandler zeroContact$getCreativeHandler() {
        IItemHandler itemHandler = new ItemStackHandler(0);
        if (!(shooter instanceof ServerPlayer player)) return itemHandler;
        LinkedHashMap<ItemStack, Integer> ammoWrap = ServerAmmoSelector.getCreativeAmmoForHeldGun(player);
        NonNullList<ItemStack> stackNonNullList = ammoWrap.keySet().stream().peek(stack -> stack.setCount(stack.getMaxStackSize())).collect(Collectors.toCollection(NonNullList::create));
        if (stackNonNullList.isEmpty()) return itemHandler;
        itemHandler = new ItemStackHandler(stackNonNullList);
        return itemHandler;
    }

    @Unique
    private int zeroContact$extractSyncTag(int neededAmount, CallbackInfoReturnable<Integer> cir, IItemHandler itemHandler, @Nullable ItemStack rigs) {
        int ammoCount = 0;
        IItemHandler modifiedHandler = ServerAmmoSelector.filteredAmmoHandler(itemHandler, AmmoInjector.getClientSelectedAmmoVariant(itemStack),itemStack);
        ammoCount = zeroContact$getAmmoCount(modifiedHandler, ammoCount, rigs);
        int actualNeededAmount = zeroContact$checkDropAmmo(neededAmount, rigs);
        zeroContact$extractAmmo(itemStack, AmmoInjector.getClientSelectedAmmoVariant(itemStack), actualNeededAmount, cir, itemHandler, modifiedHandler, ammoCount, rigs);
        return ammoCount;
    }

    @Unique
    private int zeroContact$getAmmoCount(IItemHandler itemHandler, int ammoCount, @Nullable ItemStack rigs) {
        for (int i = 0; i < itemHandler.getSlots(); ++i) {
            ItemStack checkAmmoStack = itemHandler.getStackInSlot(i);
            Item boxAmmoCount = checkAmmoStack.getItem();
            if (boxAmmoCount instanceof IAmmo iAmmo) {
                if (iAmmo.isAmmoOfGun(itemStack, checkAmmoStack)) {
                    ammoCount += checkAmmoStack.getCount();
                }
            }
        }
        return ammoCount;
    }

    @Unique
    private int zeroContact$checkDropAmmo(int neededAmount, @Nullable ItemStack rigs) {
        String clientSelected = AmmoInjector.getClientSelectedAmmoVariant(itemStack);
        if (clientSelected.isEmpty()) return neededAmount;
        Item selectedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(clientSelected));
        if (selectedItem == null) return neededAmount;
        return ServerAmmoSelector.dropAmmoFromGun(shooter, itemStack, new ItemStack(selectedItem), neededAmount, rigs);
    }

    @Unique
    private void zeroContact$extractAmmo(ItemStack gunStack, String selectedVariant, int neededAmount, CallbackInfoReturnable<Integer> cir, IItemHandler itemHandler, IItemHandler modifiedHandler, int ammoCount, @Nullable ItemStack rigs) {
        int result = this.abstractGunItem.findAndExtractInventoryAmmo(modifiedHandler, itemStack, neededAmount);
        if (itemHandler instanceof ItemStackHandler itemStackHandler && rigs != null) {
            rigs.getOrCreateTag().put("inventory", itemStackHandler.serializeNBT().getList("Items", Tag.TAG_COMPOUND));
        }
        AmmoInjector.setAmmoVariantInGun(gunStack, selectedVariant);
        cir.setReturnValue(result);
    }

}
