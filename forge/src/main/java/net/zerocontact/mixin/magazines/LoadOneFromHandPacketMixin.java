package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.config.MechanicsConfig;
import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.network.LoadOneFromHandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.compat.MagazinesCompatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LoadOneFromHandPacket.class, remap = false)
public class LoadOneFromHandPacketMixin {

    @Unique
    private static void zeroContact$updateCartridge(ItemStack ammoItem, ItemStack mag, MagazineItem magItem) {
        AmmoInjector.AmmoContext contextFromAmmo = AmmoInjector.read(ammoItem);
        if (magItem.getAmmoCount(mag) <= 0) {
            AmmoInjector.write(contextFromAmmo, mag);
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
        ItemStack heldMag = player.getMainHandItem();
        boolean looseConf = MechanicsConfig.PREFER_PLAYER_INVENTORY.get();
        if (looseConf) {
            ItemStack loose = MagazinesCompatHandler.get().getCompat()
                    .map(compat -> compat.takeLoose(heldMag, player, requiredAmmo)).orElse(ItemStack.EMPTY);
            if (!loose.isEmpty()) {
                zeroContact$updateCartridge(loose, heldMag, (MagazineItem) heldMag.getItem());
                return true;
            }
            return false;
        } else {
            ItemStack box = MagazinesCompatHandler.get().getCompat()
                    .map(compat -> compat.takeFromBox(heldMag, player, requiredAmmo)).orElse(ItemStack.EMPTY);
            if (!box.isEmpty()) {
                zeroContact$updateCartridge(box, heldMag, (MagazineItem) heldMag.getItem());
                return true;
            }
            return false;
        }
    }
}
