package net.zerocontact.mixin.magazines;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.raiiiden.taczmagazines.network.UnloadOneFromHandPacket;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.compat.MagazinesCompatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.function.Supplier;

@Mixin(value = UnloadOneFromHandPacket.class, remap = false)
public class UnloadOneFromHandPacketMixin {
    @WrapMethod(method = "lambda$handle$0")
    private static void zeroContact$transfer(Supplier<NetworkEvent.Context> ctx, Operation<Void> original) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) { original.call(ctx); return; }
        var compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        try (var ignored = compat.beginTransfer(player.getMainHandItem(), ItemStack.EMPTY, player)) {
            original.call(ctx);
        }
    }

    @WrapOperation(method = "lambda$handle$0", at = @At(value = "INVOKE",
            target = "Lcom/tacz/guns/api/item/builder/AmmoItemBuilder;build()Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack zeroContact$returnRound(AmmoItemBuilder builder, Operation<ItemStack> original) {
        ItemStack result = original.call(builder);
        return MagazinesCompatHandler.get().getCompat().map(compat -> compat.returnedRounds(result)).orElse(result);
    }
}
