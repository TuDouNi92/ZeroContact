package net.zerocontact.mixin.magazines;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.raiiiden.taczmagazines.network.LoadOneFromHandPacket;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.compat.MagazinesCompatHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(value = LoadOneFromHandPacket.class, remap = false)
public class LoadOneFromHandPacketMixin {

    @WrapMethod(method = "lambda$handle$0")
    private static void zeroContact$transfer(Supplier<NetworkEvent.Context> ctx,
                                            Operation<Void> original) {
        Player player = ctx.get().getSender();
        if (player == null) { original.call(ctx); return; }
        var compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        try (var ignored = compat.beginTransfer(player.getMainHandItem(), ItemStack.EMPTY, player)) {
            original.call(ctx);
        }
    }

    @Redirect(
            method = "lambda$handle$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/raiiiden/taczmagazines/item/MagazineAmmoSource;takeOneFromInventory(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)Z",
                    remap = false
            ),
            remap = false
    )
    private static boolean takeOneFromInv(Player player, ResourceLocation requiredAmmo) {
        return MagazinesCompatHandler.get().getCompat()
                .map(compat -> compat.takeRoundFromInventory(player.getMainHandItem(), player, requiredAmmo))
                .orElse(false);
    }
}
