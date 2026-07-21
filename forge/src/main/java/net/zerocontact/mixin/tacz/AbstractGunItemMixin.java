package net.zerocontact.mixin.tacz;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.zerocontact.events.EventUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
        rigsStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(iItemHandler -> {
            for (int i = 0; i < iItemHandler.getSlots(); i++) {
                ItemStack checkAmmoStack = iItemHandler.getStackInSlot(i);
                if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(gunItem, checkAmmoStack)) {
                    cir.setReturnValue(true);
                    return;
                }
                if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(gunItem, checkAmmoStack)) {

                    cir.setReturnValue(true);
                    return;
                }
            }
        });
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
}
