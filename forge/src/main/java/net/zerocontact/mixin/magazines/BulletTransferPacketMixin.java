package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.network.BulletTransferPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.AmmoInjector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = BulletTransferPacket.class, remap = false)
public class BulletTransferPacketMixin {
    @Unique
    private static ItemStack zeroContact$magazineItem;

    @Unique
    private static void zeroContact$updateCartridge(Slot slot, ItemStack mag, MagazineItem magItem, CallbackInfo ci) {
        AmmoInjector.AmmoContext contextFromAmmo = AmmoInjector.read(slot.getItem());
        if (magItem.getAmmoCount(mag) <= 0) {
            AmmoInjector.write(contextFromAmmo, mag);
            contextFromAmmo = AmmoInjector.read(slot.getItem());
        }
        AmmoInjector.AmmoContext contextFromMag = AmmoInjector.read(mag);
        if (!contextFromAmmo.caliber().equals(contextFromMag.caliber())) {
            ci.cancel();
        }
    }

    @Inject(method = "handleLoad",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/raiiiden/taczmagazines/item/MagazineItem;setAmmoId(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/resources/ResourceLocation;)V"), cancellable = true)
    private static void handleLoad(ServerPlayer player, AbstractContainerMenu menu, Slot slot, ItemStack mag, MagazineItem magItem, CallbackInfo ci) {
        zeroContact$updateCartridge(slot, mag, magItem, ci);
    }

    @Inject(method = "handleUnload", at = @At(value = "HEAD"))
    private static void handleUnload(ServerPlayer player, AbstractContainerMenu menu, Slot slot, ItemStack mag, MagazineItem magItem, CallbackInfo ci) {
        zeroContact$magazineItem = mag;
    }

    @ModifyVariable(
            method = "handleUnload",
            at = @At("STORE"),
            name = "bullet"
    )
    private static ItemStack replaceBullet(ItemStack bullet) {
        AmmoInjector.AmmoContext context = AmmoInjector.read(zeroContact$magazineItem);
        if (!context.isEmpty()) {
            Item ammoItem = AmmoInjector.getAmmoVariantItem(context);
            if (ammoItem != null) {
                return ammoItem.getDefaultInstance();
            }
        }
        return bullet;
    }
}
