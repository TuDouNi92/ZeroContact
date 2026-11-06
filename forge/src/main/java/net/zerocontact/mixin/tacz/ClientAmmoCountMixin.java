package net.zerocontact.mixin.tacz;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gameplay.ClientAmmoCount;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.zerocontact.caliber.compat.ReloadManager;
import net.zerocontact.compat.MagazinesCompat;
import net.zerocontact.compat.MagazinesCompatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientAmmoCount.class, remap = false, priority = 999)
public class ClientAmmoCountMixin {
    // Keep TaCZ's creative/dummy-ammo checks and shared cache; replace only the inventory scan.
    @Inject(method = "get", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;",
            remap = true), cancellable = true)
    private static void zeroContact$countReloadAmmo(LocalPlayer player, ItemStack gun, IGun iGun,
                                                    CallbackInfoReturnable<Integer> cir) {
        IItemHandler itemHandler = ReloadManager.resolveReloadInv(player).rawHandler();
        MagazinesCompat compat = MagazinesCompatHandler.get().getCompat()
                .filter(value -> value.isMagazineCompatibleWithGun(gun)).orElse(null);
        int count = 0;
        for (int i = 0; i < itemHandler.getSlots(); ++i) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (compat != null) {
                if (compat.instanceOfMagazine(stack.getItem()) && compat.isAmmoBoxOfGun(gun, stack)) {
                    long rounds = (long) Math.max(0, compat.getAmmoCount(stack)) * stack.getCount();
                    count += (int) Math.min(rounds, ClientAmmoCount.MAX_DISPLAY_COUNT - count);
                }
            } else {
                if (stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(gun, stack)) {
                    count += Math.min(stack.getCount(), ClientAmmoCount.MAX_DISPLAY_COUNT - count);
                }
                if (stack.getItem() instanceof IAmmoBox box && box.isAmmoBoxOfGun(gun, stack)) {
                    if (box.isAllTypeCreative(stack) || box.isCreative(stack)) {
                        cir.setReturnValue(ClientAmmoCount.MAX_DISPLAY_COUNT);
                        return;
                    }
                    count += Math.min(Math.max(0, box.getAmmoCount(stack)), ClientAmmoCount.MAX_DISPLAY_COUNT - count);
                }
            }
            if (count >= ClientAmmoCount.MAX_DISPLAY_COUNT) {
                break;
            }
        }
        cir.setReturnValue(count);
    }
}
