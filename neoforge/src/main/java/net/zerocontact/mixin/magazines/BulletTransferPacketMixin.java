package net.zerocontact.mixin.magazines;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.network.BulletTransferPacket;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.compat.MagazinesCompatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BulletTransferPacket.class, remap = false)
public class BulletTransferPacketMixin {
    @WrapMethod(method = "handleLoad")
    private static void zeroContact$load(ServerPlayer player, AbstractContainerMenu menu, ItemStack mag,
                                         MagazineItem magItem, Operation<Void> original) {
        var compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        if (!menu.getCarried().isEmpty() && !compat.canLoadAmmo(mag, menu.getCarried())) return;
        try (var ignored = compat.beginTransfer(mag, menu.getCarried(), player)) {
            original.call(player, menu, mag, magItem);
        }
    }

    @WrapMethod(method = "handleUnload")
    private static void zeroContact$unload(ServerPlayer player, AbstractContainerMenu menu, ItemStack mag,
                                           MagazineItem magItem, Operation<Void> original) {
        var compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        try (var ignored = compat.beginTransfer(mag, ItemStack.EMPTY, player)) {
            original.call(player, menu, mag, magItem);
        }
    }

    @WrapOperation(method = "handleUnload", at = @At(value = "INVOKE",
            target = "Lcom/tacz/guns/api/item/builder/AmmoItemBuilder;build()Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack zeroContact$returnRound(AmmoItemBuilder builder, Operation<ItemStack> original) {
        ItemStack result = original.call(builder);
        return MagazinesCompatHandler.get().getCompat().map(compat -> compat.returnedRounds(result)).orElse(result);
    }
}
