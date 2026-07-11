package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.network.LoadOneFromHandPacket;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zerocontact.caliber.AmmoInjector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(value = LoadOneFromHandPacket.class, remap = false)
public class LoadOneFromHandPacketMixin {

    @Unique
    private static ItemStack zeroContact$magazineItem;
    @Unique
    private static NonNullList<ItemStack> zeroContact$handler;

    @Unique
    private static int zeroContact$findAvailableCartridge(ItemStack magItem, NonNullList<ItemStack> handler) {
        for (int i = 0; i < handler.size(); i++) {
            ItemStack foundStack = handler.get(i);
            AmmoInjector.AmmoContext ammoContext = AmmoInjector.read(foundStack);
            AmmoInjector.AmmoContext magContext = AmmoInjector.read(magItem);
            if (ammoContext.caliber().equals(magContext.caliber())) {
                return i;
            }
        }
        return -1;
    }

    @Unique
    private static void zeroContact$updateCartridge(int slot, ItemStack mag, MagazineItem magItem, CallbackInfo ci) {
        AmmoInjector.AmmoContext contextFromAmmo = AmmoInjector.read(zeroContact$handler.get(slot));
        if (magItem.getAmmoCount(mag) <= 0) {
            AmmoInjector.write(contextFromAmmo, mag);
            contextFromAmmo = AmmoInjector.read(zeroContact$handler.get(slot));
        }
        AmmoInjector.AmmoContext contextFromMag = AmmoInjector.read(mag);
        if (!contextFromAmmo.caliber().equals(contextFromMag.caliber())) {
            ci.cancel();
        }
    }

    //Issue
    @ModifyArg(method = "lambda$handle$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;",
                    ordinal = 1))
    private static int replaceSlot(int value) {
        return zeroContact$findAvailableCartridge(zeroContact$magazineItem, zeroContact$handler);
    }

    @Inject(method = "lambda$handle$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/raiiiden/taczmagazines/item/MagazineItem;setAmmoId(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/resources/ResourceLocation;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void handle(Supplier<NetworkEvent.Context> ctx, CallbackInfo ci, ServerPlayer player, ItemStack held, MagazineItem magItem, String familyId, ResourceLocation familyAmmo, int maxCap, int current, ResourceLocation magAmmoId, int foundSlot, ResourceLocation foundAmmoId, ItemStack extras) {
        zeroContact$magazineItem = held;
        zeroContact$handler = player.getInventory().items;
        zeroContact$updateCartridge(foundSlot, zeroContact$magazineItem, magItem, ci);
    }


}
