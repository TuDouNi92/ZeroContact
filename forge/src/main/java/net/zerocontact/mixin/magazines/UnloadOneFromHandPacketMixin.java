package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.network.UnloadOneFromHandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.caliber.AmmoInjector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(value = UnloadOneFromHandPacket.class,remap = false)
public class UnloadOneFromHandPacketMixin {
    @Unique
    private static ItemStack zeroContact$magazineItem;

    @Inject(method = "lambda$handle$0",
            at= @At(
                    value = "INVOKE",
                    target = "Lcom/raiiiden/taczmagazines/item/MagazineItem;getAmmoCount(Lnet/minecraft/world/item/ItemStack;)I"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void handle(Supplier<NetworkEvent.Context> ctx, CallbackInfo ci, ServerPlayer player, ItemStack held, MagazineItem magItem, Item var4){
        zeroContact$magazineItem = held;
    }

    @ModifyVariable(method = "lambda$handle$0",
            at = @At("STORE"),
            name="bullet"
    )
    private static ItemStack replaceBullet(ItemStack bullet){
        AmmoInjector.AmmoContext context = AmmoInjector.read(zeroContact$magazineItem);
        if(!context.isEmpty()){
            Item ammoItem = AmmoInjector.getAmmoVariantItem(context);
            if(ammoItem!=null){
                return ammoItem.getDefaultInstance();
            }
        }
        return bullet;
    }
}
