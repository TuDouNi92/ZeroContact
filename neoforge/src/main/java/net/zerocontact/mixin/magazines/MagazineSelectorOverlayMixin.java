package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.client.MagazineSelectorOverlay;
import com.raiiiden.taczmagazines.item.AmmoBoxMagazineStorage;
import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.item.MagazineReloadSource;
import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.compat.ReloadManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = MagazineSelectorOverlay.class)
public class MagazineSelectorOverlayMixin {

    @Shadow(remap = false)
    @Final
    private static List<Integer> magazineSlots;

    @Shadow(remap = false)
    @Final
    private static List<ItemStack> magazineStacks;

    @Shadow(remap = false)
    private static boolean open;


    @Inject(
            method = "open",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;", remap = true),
            remap = false,
            cancellable = true)
    private static void replaceInv(LocalPlayer player, CallbackInfo ci) {
        ItemStack gun = player.getMainHandItem();
        ReloadManager.ReloadInventory inventory = ReloadManager.resolveReloadInv(player);
        for (int i = 0; i < inventory.rawHandler().getSlots(); ++i) {
            ItemStack stack = inventory.rawHandler().getStackInSlot(i);
            Item patt3207$temp = stack.getItem();
            if (patt3207$temp instanceof MagazineItem magItem) {
                if (magItem.isAmmoBoxOfGun(gun, stack) && magItem.getAmmoCount(stack) > 0) {
                    magazineSlots.add(i);
                    magazineStacks.add(stack.copy());
                }
            } else if (AmmoBoxMagazineStorage.isExternalAmmoBox(stack)) {
                IAmmoBox box = (IAmmoBox) stack.getItem();
                ItemStack boxedMagazine;
                if (box.isAllTypeCreative(stack)) {
                    boxedMagazine = MagazineReloadSource.createFullMagazineForGun(gun);
                } else {
                    boxedMagazine = AmmoBoxMagazineStorage.peekBestCompatible(stack, gun);
                }

                if (!boxedMagazine.isEmpty()) {
                    magazineSlots.add(i);
                    magazineStacks.add(boxedMagazine);
                }
            }
        }
        open = !magazineSlots.isEmpty();
        ci.cancel();
    }
}
