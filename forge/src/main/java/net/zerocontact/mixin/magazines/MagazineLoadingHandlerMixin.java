package net.zerocontact.mixin.magazines;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.raiiiden.taczmagazines.client.MagazineLoadingHandler;
import com.raiiiden.taczmagazines.network.BulletTransferPacket;
import com.raiiiden.taczmagazines.network.PacketHandler;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.compat.MagazinesCompatHandler;
import net.zerocontact.menu.BackpackContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MagazineLoadingHandler.class, remap = false)
public class MagazineLoadingHandlerMixin {
    @Shadow private static int containerSlot;
    @Shadow private static boolean unloading;

    @Shadow
    private static AbstractContainerMenu getVisibleMenu(LocalPlayer player) {
        throw new AssertionError();
    }

    @WrapMethod(method = "creativeTransferInventoryRound")
    private static void zeroContact$inventory(LocalPlayer player, Operation<Void> original) {
        AbstractContainerMenu menu = getVisibleMenu(player);
        // Backpack clicks are processed by the server even in creative mode. A client-only
        // cursor stack would disappear when the next click is reconciled with that menu.
        if (menu instanceof BackpackContainerMenu) {
            PacketHandler.CHANNEL.sendToServer(new BulletTransferPacket(containerSlot, unloading));
            return;
        }

        ItemStack magazine = containerSlot >= 0 && containerSlot < player.getInventory().items.size()
                ? player.getInventory().getItem(containerSlot) : ItemStack.EMPTY;
        var compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        // Creative ticking uses the visible menu (which can differ from containerMenu).
        ItemStack source = unloading || menu == null ? ItemStack.EMPTY : menu.getCarried();
        if (!source.isEmpty() && !compat.canLoadAmmo(magazine, source)) return;
        try (var ignored = compat.beginTransfer(magazine, source, player)) {
            original.call(player);
        }
    }

    @WrapMethod(method = {"creativeLoadOneInHand", "creativeUnloadOneFromHand"})
    private static void zeroContact$hand(LocalPlayer player, Operation<Void> original) {
        var compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        try (var ignored = compat.beginTransfer(player.getMainHandItem(), ItemStack.EMPTY, player)) {
            original.call(player);
        }
    }

    @WrapOperation(method = {"returnCreativeInventoryRound", "creativeUnloadOneFromHand"},
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/builder/AmmoItemBuilder;build()Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack zeroContact$returnRound(AmmoItemBuilder builder, Operation<ItemStack> original) {
        ItemStack result = original.call(builder);
        return MagazinesCompatHandler.get().getCompat().map(compat -> compat.returnedRounds(result)).orElse(result);
    }
}
