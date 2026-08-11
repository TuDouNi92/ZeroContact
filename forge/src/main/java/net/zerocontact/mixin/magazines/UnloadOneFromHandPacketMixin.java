package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.network.UnloadOneFromHandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.caliber.AmmoInjector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Supplier;

@Mixin(value = UnloadOneFromHandPacket.class, remap = false)
public class UnloadOneFromHandPacketMixin {

    @ModifyVariable(method = "lambda$handle$0",
            at = @At("STORE"),
            name = "bullet"
    )
    private static ItemStack replaceBullet(ItemStack bullet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return bullet;
        ItemStack heldMag = player.getMainHandItem();
        AmmoInjector.AmmoContext context = AmmoInjector.read(heldMag);
        if (!context.isEmpty()) {
            Item ammoItem = AmmoInjector.getAmmoVariantItem(context);
            if (ammoItem != null) {
                return ammoItem.getDefaultInstance();
            }
        }
        return bullet;
    }
}
