package net.zerocontact.compat;

import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.item.MagazineRegistrar;
import com.raiiiden.taczmagazines.magazine.MagazineFamilySystem;
import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MagazinesCompat {

    public static ItemStack getCompatibleMag(ItemStack gunStack) {
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

    public static boolean isMagazineCompatibleWithGun(ItemStack gunStack) {
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) return false;
        ResourceLocation gunId = gun.getGunId(gunStack);
        String familyId = MagazineFamilySystem.getFamilyForGun(gunId);
        return familyId != null && !familyId.isEmpty();
    }
}
