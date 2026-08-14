package net.zerocontact.compat;

import com.raiiiden.taczmagazines.capability.GunMagazineProvider;
import com.raiiiden.taczmagazines.config.MechanicsConfig;
import com.raiiiden.taczmagazines.item.AmmoBoxMagazineStorage;
import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.item.MagazineRegistrar;
import com.raiiiden.taczmagazines.magazine.MagazineFamilySystem;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ICartridgeHolder;
import net.zerocontact.caliber.AmmoInjector;

import java.util.List;

import static com.raiiiden.taczmagazines.item.MagazineAmmoSource.*;

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
        List<String> extFamilies = MagazineFamilySystem.getExtendedFamiliesForBaseFamily(familyId);
        GunData gunData = TimelessAPI.getCommonGunIndex(gunId).map(CommonGunIndex::getGunData).orElse(null);
        if (gunData == null) return magStack;
        int extLevel = AttachmentDataUtils.getMagExtendLevel(gunStack, gunData);
        if (familyId == null) return magStack;
        String familyIdWithExt = extFamilies.stream().filter(s -> MagazineFamilySystem.getExtLevelForFamily(s) == extLevel).findAny().orElse("");
        return gunStack.getCapability(GunMagazineProvider.GUN_MAGAZINE).map(cap -> {
            ItemStack stored = cap.getStoredMagazine();
            Item magItem = MagazineRegistrar.MAGAZINE.get();
            if (stored.isEmpty()) {
                String id = familyId;
                if (!familyIdWithExt.isEmpty()) {
                    id = familyIdWithExt;
                }
                stored = MagazineItem.createMagazineByFamily(magItem, id, MagazineFamilySystem.getCapacityForFamily(id), gunData.getAmmoId());
            }
            return stored;
        }).orElse(ItemStack.EMPTY);

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

    public ItemStack takeLoose(ItemStack mag, Player player, ResourceLocation requiredAmmo) {
        AmmoInjector.AmmoContext magContext = AmmoInjector.read(mag);
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof IAmmo)
                    || compatibleAmmoId(stack, requiredAmmo) == null) {
                continue;
            }
            AmmoInjector.AmmoContext ammoContext = AmmoInjector.read(stack);
            if (!magContext.isEmpty() && !ammoContext.equals(magContext)) {
                continue;
            }
            ItemStack result = stack.copy();
            result.setCount(1);
            stack.shrink(1);
            player.getInventory().setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    public ItemStack takeFromBox(ItemStack mag, Player player, ResourceLocation requiredAmmo) {
        ItemStack result = ItemStack.EMPTY;
        if (MechanicsConfig.LOAD_MAGAZINES_FROM_AMMO_BOXES.get()) {
            for (ItemStack stack : player.getInventory().items) {
                if (!AmmoBoxMagazineStorage.isExternalAmmoBox(stack) || compatibleAmmoId(stack, requiredAmmo) == null || available(stack) <= 0) {
                    continue;
                }
                AmmoInjector.AmmoContext magContext = AmmoInjector.read(mag);
                AmmoInjector.AmmoContext boxContext = AmmoInjector.read(stack);
                if (!boxContext.equals(magContext) && !magContext.isEmpty()) {
                    continue;
                }
                result = stack.copy();
                consume(stack, 1);
                player.getInventory().setChanged();
                return result;
            }
        }
        return result;
    }
}
