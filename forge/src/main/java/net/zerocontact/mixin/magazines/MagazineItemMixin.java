package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.item.MagazineItem;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.AmmoInjector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MagazineItem.class, remap = false)
public abstract class MagazineItemMixin {
    @Shadow
    public abstract int getAmmoCount(ItemStack magazine);

    @Unique
    private void zeroContact$updateCartridge(ItemStack mag, Slot slot, CallbackInfoReturnable<Boolean> cir) {
        AmmoInjector.AmmoContext contextFromAmmo = AmmoInjector.read(slot.getItem());
        if (getAmmoCount(mag) <= 0) {
            AmmoInjector.write(contextFromAmmo, mag);
            contextFromAmmo = AmmoInjector.read(slot.getItem());
        }
        AmmoInjector.AmmoContext contextFromMag = AmmoInjector.read(mag);
        if (!contextFromAmmo.caliber().equals(contextFromMag.caliber())) {
            cir.cancel();
        }
    }

    @Inject(method = "overrideStackedOnOther",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/raiiiden/taczmagazines/item/MagazineItem;setAmmoId(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/resources/ResourceLocation;)V"),
            cancellable = true
    )

    //Called when holds the magazine and right-clicks on ammo
    public void overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player, CallbackInfoReturnable<Boolean> cir) {
        zeroContact$updateCartridge(stack, slot, cir);
    }

    @Unique
    private void zeroContact$updateCartridge(ItemStack mag, ItemStack ammoStack, CallbackInfoReturnable<Boolean> cir) {
        AmmoInjector.AmmoContext contextFromAmmo = AmmoInjector.read(ammoStack);
        if (getAmmoCount(mag) <= 0) {
            AmmoInjector.write(contextFromAmmo, mag);
            contextFromAmmo = AmmoInjector.read(ammoStack);
        }
        AmmoInjector.AmmoContext contextFromMag = AmmoInjector.read(mag);
        if (!contextFromAmmo.caliber().equals(contextFromMag.caliber())) {
            cir.cancel();
        }
    }

    @Inject(method = "overrideOtherStackedOnMe",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/raiiiden/taczmagazines/item/MagazineItem;getAmmoId(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/resources/ResourceLocation;",
                    ordinal = 1),
            cancellable = true)
    public void overrideOtherStackedOnMeLoad(ItemStack magazine, ItemStack heldStack, Slot slot, ClickAction action, Player player, SlotAccess heldAccess, CallbackInfoReturnable<Boolean> cir) {
        zeroContact$updateCartridge(magazine, heldStack, cir);
    }
}
