package net.zerocontact.mixin;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(ModernKineticGunScriptAPI.class)
public class TaczModernKinetGunScriptAPIMixin {
    @Shadow(remap = false)
    private LivingEntity shooter;
    @Shadow(remap = false)
    private AbstractGunItem abstractGunItem;
    @Shadow(remap = false)
    private ItemStack itemStack;

    @Inject(method = "consumeAmmoFromPlayer", at = @At("HEAD"), remap = false, cancellable = true)
    public void zeroContact$consumeAmmoFromPlayerRigs(int neededAmount, CallbackInfoReturnable<Integer> cir) {
        CuriosApi.getCuriosInventory(shooter).ifPresent(inv -> {
            inv.getStacksHandler("rigs").ifPresent(stacksHandler -> {
                ItemStack rigs = stacksHandler.getStacks().getStackInSlot(0);
                rigs.getCapability(ForgeCapabilities.ITEM_HANDLER, null).map(itemHandler -> {
                    int ammoCount = 0;
                    for (int i = 0; i < itemHandler.getSlots(); ++i) {
                        ItemStack checkAmmoStack = itemHandler.getStackInSlot(i);
                        Item boxAmmoCount = checkAmmoStack.getItem();
                        if (boxAmmoCount instanceof IAmmo iAmmo) {
                            if (iAmmo.isAmmoOfGun(itemStack, checkAmmoStack)) {
                                ammoCount += checkAmmoStack.getCount();
                            }
                        }
                    }
                    if (ammoCount >= neededAmount) {
                        int result = this.abstractGunItem.findAndExtractInventoryAmmo(itemHandler, itemStack, neededAmount);
                        if (itemHandler instanceof ItemStackHandler itemStackHandler) {
                            rigs.getOrCreateTag().put("inventory", itemStackHandler.serializeNBT().getList("Items", Tag.TAG_COMPOUND));
                            cir.setReturnValue(result);
                        }
                    }
                    return ammoCount;
                });
            });
        });
    }
}
