package net.zerocontact.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import com.raiiiden.taczmagazines.item.MagazineItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.zerocontact.compat.magazines.MagazineRoundStack;
import net.zerocontact.compat.magazines.MagazineRoundStack.Round;

/** Boundary regressions that need neither a world nor loaded gun packs. */
public final class MagazinesCompatNullSafetyTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        MagazinesCompat compat = new MagazinesCompat();
        for (ItemStack absent : new ItemStack[]{null, ItemStack.EMPTY}) {
            check(!compat.managesGun(absent), "absent gun is unmanaged");
            check(!compat.isMagazineCompatibleWithGun(absent), "absent gun has no magazine family");
            check(compat.getCompatibleMag(absent).isEmpty(), "absent gun produces no magazine");
            check(compat.getMag(absent).isEmpty(), "absent gun has no stored magazine");
            check(compat.getAmmoCount(absent) == 0, "absent magazine has no ammo");
            check(!compat.canLoadAmmo(absent, absent), "absent ammo cannot load");
            check(!compat.hasUsableMagazine(null, absent), "missing inventory has no magazine");
            check(!compat.hasCompatibleMagazineInBox(absent, absent), "absent box has no magazine");
            check(!compat.isAmmoBoxOfGun(absent, absent), "absent box is incompatible");
            check(compat.prepareCreativeMagazine(null, absent, 0, absent).isEmpty(),
                    "missing creative selection produces no magazine");
            check(!compat.takeRoundFromInventory(absent, null, null), "missing player cannot supply ammo");
            check(compat.takeLoose(absent, null, null).isEmpty(), "missing inventory has no loose ammo");
            check(compat.takeFromBox(absent, null, null).isEmpty(), "missing inventory has no ammo box");
            compat.initializeMagazine(absent);
            compat.updateMagazineCount(absent, 0, 1);
            compat.captureChamber(absent);
            compat.beforeStoreMagazine(absent, absent);
            check(compat.beginShot(absent, 7) == 7, "unmanaged shot retains original projectile count");
            compat.consumedShot(absent, 1, true);
            compat.removedFromMagazine(absent, 1, 1);
            compat.setChamber(absent, true);
            compat.setChamber(absent, false);
            compat.endShot(absent);
            compat.setVariantFromMag(absent, absent, null);
            try (var outer = compat.beginTransfer(absent, absent, null)) {
                try (var inner = compat.beginTransfer(absent, absent, null)) {
                    compat.recordLoadedRound(absent, absent);
                    check(compat.returnedRounds(absent).isEmpty(), "absent unload has no returned ammo");
                }
                compat.updateMagazineCount(absent, 1, 0);
            }
        }
        ForgeRegistry<Item> registry = (ForgeRegistry<Item>) ForgeRegistries.ITEMS;
        registry.unfreeze();
        // Standalone bootstrap also freezes Forge's separate vanilla registry wrapper.
        var unfreeze = BuiltInRegistries.ITEM.getClass().getMethod("unfreeze");
        unfreeze.setAccessible(true);
        unfreeze.invoke(BuiltInRegistries.ITEM);
        MagazineItem item;
        try {
            item = new MagazineItem(new Item.Properties());
            registry.register(new ResourceLocation("zerocontact_test", "magazine"), item);
        } finally {
            registry.freeze();
            BuiltInRegistries.ITEM.freeze();
        }
        ItemStack unresolved = new ItemStack(item);
        unresolved.getOrCreateTag().putInt("AmmoCount", 2);
        unresolved.getOrCreateTag().putInt("MaxCapacity", 30);
        CompoundTag originalTag = unresolved.getTag().copy();
        compat.initializeMagazine(unresolved);
        check(originalTag.equals(unresolved.getTag()), "missing ammo metadata preserves magazine NBT");
        try (var transfer = compat.beginTransfer(unresolved, null, null)) {
            item.setAmmoCount(unresolved, 3);
            compat.updateMagazineCount(unresolved, 2, 3);
            check(originalTag.equals(unresolved.getTag()), "failed recovery restores native count update");
        }

        ItemStack persisted = new ItemStack(item);
        persisted.getOrCreateTag().putInt("AmmoCount", 2);
        persisted.getOrCreateTag().putInt("MaxCapacity", 30);
        ListTag rounds = new ListTag();
        Round fmj = new Round("tacz:556x45", "zerocontact:556_fmj");
        Round ap = new Round("tacz:556x45", "zerocontact:556_ap");
        rounds.add(fmj.save());
        rounds.add(ap.save());
        persisted.getOrCreateTag().put(MagazineRoundStack.TAG, rounds);
        compat.initializeMagazine(persisted);
        check(item.getAmmoCount(persisted) == 2 && item.getAmmoId(persisted).toString().equals(ap.ammoId()),
                "persisted cartridges recover without family or legacy metadata");
        check(ap.equals(Round.load(persisted.getTag().getCompound("ai_ammo"))),
                "recovered magazine keeps its top variant");
        compat.setVariantFromMag(ItemStack.EMPTY, persisted, null);
        check(item.getAmmoCount(persisted) == 2, "unmanaged gun leaves persisted magazine intact");
        try (var transfer = compat.beginTransfer(persisted, null, null)) {
            item.setAmmoCount(persisted, 1);
            compat.updateMagazineCount(persisted, 2, 1);
            check(item.getAmmoCount(persisted) == 1
                    && fmj.equals(Round.load(persisted.getTag().getCompound("ai_ammo"))),
                    "partial unload works without family metadata");
        }
        System.out.println("MagazinesCompat null safety: " + checks + " checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
