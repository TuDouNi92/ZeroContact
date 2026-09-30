package net.zerocontact.mixin.magazines;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.compat.MagazinesCompatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Registered only by the optional magazines config; all hooks gate on a stored detachable magazine. */
@Mixin(value = ModernKineticGunScriptAPI.class, remap = false)
public class MagazineGunScriptAPIMixin {
    @Shadow private ItemStack itemStack;
    @Shadow private AbstractGunItem abstractGunItem;

    @WrapMethod(method = "consumeAmmoFromPlayer")
    private int zeroContact$reload(int neededAmount, Operation<Integer> original) {
        MagazinesCompatHandler.get().getCompat().ifPresent(compat -> compat.captureChamber(itemStack));
        return original.call(neededAmount);
    }

    @WrapMethod(method = "lambda$shootOnce$2")
    private boolean zeroContact$shot(boolean consumeAmmo, GunData gunData, int bulletAmount, BulletData bulletData,
                                     IGunOperator gunOperator, float shotDamageMultiplier, float processedSpeed, float inaccuracy,
                                     int soundDistance, boolean useSilenceSound, Operation<Boolean> original) {
        var compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        if (!compat.managesGun(itemStack)) {
            return original.call(consumeAmmo, gunData, bulletAmount, bulletData, gunOperator, shotDamageMultiplier, processedSpeed, inaccuracy, soundDistance, useSilenceSound);
        }
        int amount = compat.beginShot(itemStack, bulletAmount);
        try {
            return original.call(consumeAmmo, gunData, amount, bulletData, gunOperator, shotDamageMultiplier, processedSpeed, inaccuracy, soundDistance, useSilenceSound);
        } finally {
            compat.endShot(itemStack);
        }
    }

    @WrapMethod(method = "reduceAmmoOnce")
    private boolean zeroContact$consume(Operation<Boolean> original) {
        var compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        if (!compat.managesGun(itemStack)) return original.call();
        compat.captureChamber(itemStack);
        int previous = abstractGunItem.getCurrentAmmoCount(itemStack);
        boolean chambered = abstractGunItem.hasBulletInBarrel(itemStack);
        boolean consumed = original.call();
        if (consumed) compat.consumedShot(itemStack, previous, chambered);
        return consumed;
    }

    @WrapMethod(method = "removeAmmoFromMagazine")
    private int zeroContact$feed(int amount, Operation<Integer> original) {
        var compat = MagazinesCompatHandler.get().getCompat().orElseThrow();
        boolean managed = compat.managesGun(itemStack);
        int previous = managed ? abstractGunItem.getCurrentAmmoCount(itemStack) : 0;
        int removed = original.call(amount);
        if (managed) compat.removedFromMagazine(itemStack, previous, removed);
        return removed;
    }

    @WrapMethod(method = "setAmmoInBarrel")
    private void zeroContact$chamber(boolean ammoInBarrel, Operation<Void> original) {
        original.call(ammoInBarrel);
        MagazinesCompatHandler.get().getCompat().ifPresent(compat -> compat.setChamber(itemStack, ammoInBarrel));
    }
}
