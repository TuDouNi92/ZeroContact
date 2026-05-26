package net.zerocontact.mixin;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.zerocontact.events.AmmoInjector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(AbstractGunItem.class)
public class TaczAbstractGunItemMixin {
    @Inject(method = "canReload",at = @At("RETURN"),remap = false, cancellable = true)
    public void zeroContact$canReload(LivingEntity shooter, ItemStack gunItem, CallbackInfoReturnable<Boolean> cir){
        CuriosApi.getCuriosInventory(shooter).ifPresent(itemHandler->{
            itemHandler.getStacksHandler("rigs").ifPresent(stacksHandler -> {
                ItemStack backpack =stacksHandler.getStacks().getStackInSlot(0);
                backpack.getCapability(ForgeCapabilities.ITEM_HANDLER,null).map(iItemHandler->{
                    for(int i=0;i<iItemHandler.getSlots();i++){
                        ItemStack ammoStack = iItemHandler.getStackInSlot(i);
                        if(ammoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(gunItem,ammoStack)){
                            cir.setReturnValue(true);
                            return true;
                        };
                    }
                    return false;
                });
            });
        });
    }
    @Inject(method = "findAndExtractInventoryAmmo",at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/IAmmo;isAmmoOfGun(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",shift = At.Shift.AFTER),remap = false,locals = LocalCapture.CAPTURE_FAILHARD)
    public void zeroContact$findAndExtractInventoryAmmo(IItemHandler itemHandler, ItemStack gunItem, int needAmmoCount, CallbackInfoReturnable<Integer> cir, int cnt, int i, ItemStack checkAmmoStack, IAmmo iAmmo, Item var8){
        AmmoInjector.copyTags(checkAmmoStack,gunItem);
    }
}
