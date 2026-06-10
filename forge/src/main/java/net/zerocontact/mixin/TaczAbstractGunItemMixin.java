package net.zerocontact.mixin;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.zerocontact.events.EventUtil;
import net.zerocontact.network.ServerAmmoSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractGunItem.class)
public class TaczAbstractGunItemMixin {
    @Unique
    private LivingEntity zeroContact$shooter;

    @Inject(method = "canReload", at = @At("RETURN"), remap = false, cancellable = true)
    public void zeroContact$canReload(LivingEntity shooter, ItemStack gunItem, CallbackInfoReturnable<Boolean> cir) {
        this.zeroContact$shooter = shooter;
        ItemStack rigsStack = EventUtil.getCuriosStackFirst(shooter, "rigs");
        rigsStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).map(iItemHandler -> {
            for (int i = 0; i < iItemHandler.getSlots(); i++) {
                ItemStack ammoStack = iItemHandler.getStackInSlot(i);
                if (ammoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(gunItem, ammoStack)) {
                    boolean isNeededAmmo = ServerAmmoSelector.isNeededAmmo(ammoStack, gunItem);
                    cir.setReturnValue(isNeededAmmo);
                    return isNeededAmmo;
                }
                ;
            }
            return false;
        });
    }

    @ModifyArg(method = "findAndExtractInventoryAmmo", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/IItemHandler;extractItem(IIZ)Lnet/minecraft/world/item/ItemStack;"), remap = false)
    public boolean zeroContact$findAndExtractInventoryAmmo(boolean simulate) {
        if (zeroContact$shooter instanceof ServerPlayer player && player.isCreative()) {
            return true;
        }
        return simulate;
    }
}
