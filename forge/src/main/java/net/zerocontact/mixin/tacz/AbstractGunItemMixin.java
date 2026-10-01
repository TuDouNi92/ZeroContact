package net.zerocontact.mixin.tacz;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.zerocontact.api.caliber.ICartridgeHolder;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.compat.MagazineReloadCheck;
import net.zerocontact.caliber.compat.ReloadManager;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.compat.MagazinesCompatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractGunItem.class)
public abstract class AbstractGunItemMixin implements IGun {

    @Shadow(remap = false)
    public abstract boolean useInventoryAmmo(ItemStack gun);

    @WrapMethod(method = "canReload", remap = false)
    private boolean zeroContact$checkMagazineReloadInventory(LivingEntity shooter, ItemStack gunItem,
                                                             Operation<Boolean> original) {
        // The magazine mod's canReload injector queries the player's capability directly.
        return MagazineReloadCheck.withShooter(shooter, () -> original.call(shooter, gunItem));
    }


    @Inject(method = "canReload", at = @At("HEAD"), remap = false, cancellable = true)
    public void zeroContact$canReload(LivingEntity shooter, ItemStack gunItem, CallbackInfoReturnable<Boolean> cir) {
        ResourceLocation gunId = this.getGunId(gunItem);
        CommonGunIndex gunIndex = TimelessAPI.getCommonGunIndex(gunId).orElse(null);
        if (gunIndex == null) return;

        ReloadManager.ReloadInventory zeroContact$reloadInventory = ReloadManager.resolveReloadInv(shooter);
        boolean magazineCompat = MagazinesCompatHandler.get().getCompat().map(compat->compat.isMagazineCompatibleWithGun(gunItem)).orElse(false);

        if (!magazineCompat) {

            if (this.useInventoryAmmo(gunItem) || gunIndex.getGunData().getReloadData().isInfinite() || this.useDummyAmmo(gunItem)) {
                return;
            }

            for (int i = 0; i < zeroContact$reloadInventory.rawHandler().getSlots(); i++) {
                if (zeroContact$sameOrSelectedCaliber(shooter, gunItem, zeroContact$reloadInventory.rawHandler(), i)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
            cir.setReturnValue(false);
            zeroContact$sendFailMsg(shooter);
        }
    }

    @Inject(method = "hasInventoryAmmo", remap = false, at = @At("HEAD"), cancellable = true)
    private void replaceAmmoCheck(LivingEntity shooter, ItemStack gun, boolean needCheckAmmo, CallbackInfoReturnable<Boolean> cir) {
        if (!this.useInventoryAmmo(gun)) {
            cir.setReturnValue(false);
        } else if (!needCheckAmmo) {
            cir.setReturnValue(true);
        } else if (this.useDummyAmmo(gun)) {
            cir.setReturnValue(this.getDummyAmmoAmount(gun) > 0);
        } else {
            ReloadManager.ReloadInventory reloadInv = ReloadManager.resolveReloadInv(shooter);
            for (int i = 0; i < reloadInv.rawHandler().getSlots(); i++) {
                if (zeroContact$sameOrSelectedCaliber(shooter, gun, reloadInv.rawHandler(), i)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
            zeroContact$sendFailMsg(shooter);
            cir.setReturnValue(false);
        }
        cir.cancel();
    }

    @Unique
    private static boolean zeroContact$sameOrSelectedCaliber(LivingEntity shooter, ItemStack gunItem, IItemHandler iItemHandler, int i) {
        ItemStack checkAmmoStack = iItemHandler.getStackInSlot(i);
        if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(gunItem, checkAmmoStack)) {

            AmmoInjector.AmmoContext gunCtx = AmmoInjector.read(gunItem);
            AmmoInjector.AmmoContext ammoCtx = AmmoInjector.read(checkAmmoStack);
            String selectedVariant = gunItem.getCapability(CapabilityRegistries.CARTRIDGE).map(cap ->
                    cap.getClientSelectedAmmoVariant(gunItem)).orElse("");

            if (gunCtx.isEmpty()) return false;

            return gunCtx.caliber().equals(ammoCtx.caliber()) || selectedVariant.equals(ammoCtx.caliber().variant());
        }
        if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(gunItem, checkAmmoStack)) {

            AmmoInjector.AmmoContext gunCtx = AmmoInjector.read(gunItem);
            AmmoInjector.AmmoContext ammoCtx = AmmoInjector.read(checkAmmoStack);
            String selectedCaliber = gunItem.getCapability(CapabilityRegistries.CARTRIDGE).map(cap ->
                    cap.getClientSelectedAmmoVariant(gunItem)).orElse("");
            if (gunCtx.isEmpty()) return false;
            if (iAmmoBox.isAllTypeCreative(checkAmmoStack)) return true;
            return gunCtx.caliber().equals(ammoCtx.caliber()) || selectedCaliber.equals(ammoCtx.caliber().variant());
        }
        return false;
    }

    @Unique
    private static void zeroContact$sendFailMsg(LivingEntity shooter) {
        if (shooter instanceof Player player) {
            if (player.isCreative()) return;
            player.displayClientMessage(
                    Component.translatable("msg.zerocontact.no_matching_ammunition").withStyle(ChatFormatting.RED)
                    , true
            );
        }
    }

    @ModifyArg(method = "findAndExtractInventoryAmmo", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/IItemHandler;extractItem(IIZ)Lnet/minecraft/world/item/ItemStack;"), remap = false)
    public boolean zeroContact$findAndExtractInventoryAmmo(boolean simulate, @Local(argsOnly = true) ItemStack gunItem) {
        return gunItem.getCapability(CapabilityRegistries.CARTRIDGE).map(ICartridgeHolder::getCreativeHandling).orElse(simulate);
    }

    @ModifyVariable(
            method = "doBulletSpread",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1,
            remap = false
    )
    private float zeroContact$modifyInaccuracy(float original, @Local(argsOnly = true) LivingEntity shooter) {
        ItemStack gunStack = shooter.getMainHandItem();
        if (IGun.getIGunOrNull(gunStack) == null) return original;
        return gunStack.getCapability(CapabilityRegistries.CARTRIDGE).map(
                cap -> {
                    InaccuracyType type = InaccuracyType.getInaccuracyType(shooter);
                    return cap.getInaccuracy(gunStack).get(type);
                }
        ).orElse(original);
    }
}
