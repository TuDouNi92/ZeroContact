package net.zerocontact.mixin.tacz;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.events.EventUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractGunItem.class)
public class AbstractGunItemMixin {
    @Unique
    private LivingEntity zeroContact$shooter;

    @Inject(method = "canReload", at = @At("HEAD"), remap = false, cancellable = true)
    public void zeroContact$canReload(LivingEntity shooter, ItemStack gunItem, CallbackInfoReturnable<Boolean> cir) {
        this.zeroContact$shooter = shooter;
        ItemStack rigsStack = EventUtil.getCuriosStackFirst(shooter, "rigs");
        //Essential check since a NPE occurred here but in MinecraftOrRainbow client.
        if (rigsStack == null) return;
        zeroContact$grantVanillaFullAmmoReload(gunItem, cir);
        if (rigsStack.isEmpty()) {
            shooter.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(iItemHandler -> {
                for (int i = 0; i < iItemHandler.getSlots(); i++) {
                    if (zeroContact$sameOrSelectedCaliber(shooter, gunItem, cir, iItemHandler, i)) return;
                }
                zeroContact$sendFailMsg(shooter);
            });
            return;
        }
        rigsStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(iItemHandler -> {
            for (int i = 0; i < iItemHandler.getSlots(); i++) {
                if (zeroContact$sameOrSelectedCaliber(shooter, gunItem, cir, iItemHandler, i)) return;
            }
            zeroContact$sendFailMsg(shooter);
        });
    }

    @Unique
    private static boolean zeroContact$sameOrSelectedCaliber(LivingEntity shooter, ItemStack gunItem, CallbackInfoReturnable<Boolean> cir, IItemHandler iItemHandler, int i) {
        ItemStack checkAmmoStack = iItemHandler.getStackInSlot(i);
        if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(gunItem, checkAmmoStack)) {

            AmmoInjector.AmmoContext gunCtx = AmmoInjector.read(gunItem);
            AmmoInjector.AmmoContext ammoCtx = AmmoInjector.read(checkAmmoStack);
            String selectedCaliber = gunItem.getCapability(CapabilityRegistries.CARTRIDGE).map(cap ->
                    cap.getClientSelectedAmmoVariant(gunItem)).orElse("");

            if (gunCtx.isEmpty()) return false;

            boolean sameCaliber = gunCtx.caliber().equals(ammoCtx.caliber()) || selectedCaliber.equals(ammoCtx.caliber().variant());

            if (!sameCaliber) {
                cir.setReturnValue(false);
                return false;
            }
            cir.setReturnValue(true);
            return true;
        }
        if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(gunItem, checkAmmoStack)) {

            AmmoInjector.AmmoContext gunCtx = AmmoInjector.read(gunItem);
            AmmoInjector.AmmoContext ammoCtx = AmmoInjector.read(checkAmmoStack);
            String selectedCaliber = gunItem.getCapability(CapabilityRegistries.CARTRIDGE).map(cap ->
                    cap.getClientSelectedAmmoVariant(gunItem)).orElse("");

            if (gunCtx.isEmpty()) return false;

            boolean sameCaliber = gunCtx.caliber().equals(ammoCtx.caliber()) || selectedCaliber.equals(ammoCtx.caliber().variant());

            if (!sameCaliber) {
                cir.setReturnValue(false);
                return false;
            }
            cir.setReturnValue(true);
            return true;
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

    @Unique
    private static void zeroContact$grantVanillaFullAmmoReload(ItemStack gunItem, CallbackInfoReturnable<Boolean> cir) {
        IGun gun = IGun.getIGunOrNull(gunItem);
        if (gun == null) {
            cir.setReturnValue(false);
            return;
        }
        ResourceLocation gunId = gun.getGunId(gunItem);
        CommonGunIndex gunIndex = TimelessAPI.getCommonGunIndex(gunId).orElse(null);
        if (gunIndex == null) {
            cir.setReturnValue(false);
            return;
        }
        int currentAmmoCount = gun.getCurrentAmmoCount(gunItem);
        int maxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(gunItem, gunIndex.getGunData());
        if (currentAmmoCount == maxAmmoCount) {
            cir.setReturnValue(true);
        }
    }

    @ModifyArg(method = "findAndExtractInventoryAmmo", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/IItemHandler;extractItem(IIZ)Lnet/minecraft/world/item/ItemStack;"), remap = false)
    public boolean zeroContact$findAndExtractInventoryAmmo(boolean simulate) {
        if (zeroContact$shooter instanceof ServerPlayer player && player.isCreative()) {
            return true;
        }
        return simulate;
    }

    @ModifyVariable(
            method = "doBulletSpread",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1,
            remap = false
    )
    private float zeroContact$modifyInaccuracy(float original) {
        ItemStack gunStack = zeroContact$shooter.getMainHandItem();
        if (IGun.getIGunOrNull(gunStack) == null) return original;
        return gunStack.getCapability(CapabilityRegistries.CARTRIDGE).map(
                cap -> {
                    InaccuracyType type = InaccuracyType.getInaccuracyType(zeroContact$shooter);
                    return cap.getInaccuracy(gunStack).get(type);
                }
        ).orElse(original);
    }
}
