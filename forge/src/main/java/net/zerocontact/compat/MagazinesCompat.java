package net.zerocontact.compat;

import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.item.MagazineRegistrar;
import com.raiiiden.taczmagazines.magazine.MagazineFamilySystem;
import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ICartridgeHolder;
import net.zerocontact.caliber.AmmoInjector;

public class MagazinesCompat {

    public boolean instanceOfMagazine(Item object) {
        return object instanceof MagazineItem;
    }

    public ItemStack getCompatibleMag(ItemStack gunStack) {
        ItemStack magStack = ItemStack.EMPTY;
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) return magStack;
        ResourceLocation gunId = gun.getGunId(gunStack);
        String familyId = MagazineFamilySystem.getFamilyForGun(gunId);
        if (familyId == null) return magStack;
        Item magItem = MagazineRegistrar.MAGAZINE.get();
        magStack = MagazineItem.createMagazineByFamily(magItem, familyId, MagazineFamilySystem.getCapacityForFamily(familyId));
        return magStack;
    }

    public boolean isMagazineCompatibleWithGun(ItemStack gunStack) {
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) return false;
        ResourceLocation gunId = gun.getGunId(gunStack);
        String familyId = MagazineFamilySystem.getFamilyForGun(gunId);
        return familyId != null && !familyId.isEmpty();
    }

    public void setVariantFromMag(ItemStack gunStack, ItemStack magStack, ICartridgeHolder cap) {
        if (magStack.getItem() instanceof MagazineItem magazineItem) {
            if (magazineItem.isAmmoBoxOfGun(gunStack, magStack)) {
                AmmoInjector.AmmoContext context = AmmoInjector.read(magStack);
                if (context.isEmpty()) {
                    AmmoInjector.setEntityGunContext(gunStack);
                } else {
                    cap.setAmmoVariantInGun(gunStack, context.caliber().variant());
                }
            }
        }
    }
}
