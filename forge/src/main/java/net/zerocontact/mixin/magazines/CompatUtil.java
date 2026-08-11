package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.config.MechanicsConfig;
import com.raiiiden.taczmagazines.item.AmmoBoxMagazineStorage;
import com.tacz.guns.api.item.IAmmo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.AmmoInjector;

import static com.raiiiden.taczmagazines.item.MagazineAmmoSource.*;

public class CompatUtil {

    public static ItemStack takeLoose(ItemStack mag, Player player, ResourceLocation requiredAmmo) {
        ItemStack result = ItemStack.EMPTY;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof IAmmo && compatibleAmmoId(stack, requiredAmmo) != null) {
                AmmoInjector.AmmoContext magContext = AmmoInjector.read(mag);
                AmmoInjector.AmmoContext ammoContext = AmmoInjector.read(stack);
                if (ammoContext.equals(magContext) || magContext.isEmpty()) {
                    result = stack.copy();
                    stack.shrink(1);
                    player.getInventory().setChanged();
                }
                return result;
            }
        }

        return result;
    }

    public static ItemStack takeFromBox(ItemStack mag, Player player, ResourceLocation requiredAmmo) {
        ItemStack result = ItemStack.EMPTY;
        if (MechanicsConfig.LOAD_MAGAZINES_FROM_AMMO_BOXES.get()) {
            for (ItemStack stack : player.getInventory().items) {
                if (AmmoBoxMagazineStorage.isExternalAmmoBox(stack) && compatibleAmmoId(stack, requiredAmmo) != null && available(stack) > 0) {
                    AmmoInjector.AmmoContext magContext = AmmoInjector.read(mag);
                    AmmoInjector.AmmoContext boxContext = AmmoInjector.read(stack);
                    if (boxContext.equals(magContext) || magContext.isEmpty()) {
                        result = stack.copy();
                        consume(stack, 1);
                        player.getInventory().setChanged();
                    }
                    return result;
                }
            }
        }
        return result;
    }

}
