package net.zerocontact.caliber.damage;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.EntityUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.events.EventUtil;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class HitUtil {

    public static boolean isIncidentAngleValid(LivingEntity lv, DamageSource source) {
        double incidentAngle = getAngle(lv, source);
        double incidentAngleAbs = Math.abs(incidentAngle);
        if (incidentAngle != -361) {
            return (Math.abs(incidentAngleAbs - 90) <= 30) && (Math.abs(incidentAngleAbs - 90) >= 10);
        }
        return false;
    }

    public static ItemStack[] getHitBodyPartStack(LivingEntity lv, DamageSource source) {
        double incidentAngleAbs = Math.abs(getAngle(lv, source));
        AtomicReference<ItemStack[]> defenseStacks = new AtomicReference<>(new ItemStack[]{ItemStack.EMPTY});
        if (lv.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof IEquipmentTypeTag tag && tag.getArmorType().equals(IEquipmentTypeTag.EquipmentType.ARMOR)) {
            defenseStacks.set(new ItemStack[]{lv.getItemBySlot(EquipmentSlot.CHEST)});
            return defenseStacks.get();
        }
        ItemStack frontPlate = EventUtil.getCuriosStackFirst(lv, "front_plate");
        ItemStack backPlate = EventUtil.getCuriosStackFirst(lv, "back_plate");
        ItemStack plateStack = ItemStack.EMPTY;
        if (incidentAngleAbs != 361) {
            if (incidentAngleAbs > 90) {
                plateStack = frontPlate;
            } else {
                plateStack = backPlate;
            }
        }
        if (lv.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof IEquipmentTypeTag tag && tag.getArmorType().equals(IEquipmentTypeTag.EquipmentType.PLATE_CARRIER)) {
            defenseStacks.set(new ItemStack[]{plateStack, lv.getItemBySlot(EquipmentSlot.CHEST)});
        }
        return defenseStacks.get();
    }

    private static double getAngle(LivingEntity lv, DamageSource source) {
        double incidentAngle = -361;
        if (source.getEntity() != null) {
            double sourceDx = lv.getX() - source.getEntity().getX();
            double sourceDz = lv.getZ() - source.getEntity().getZ();
            double lookDx = lv.getLookAngle().x;
            double lookDz = lv.getLookAngle().z;
            double lookAngle = Math.toDegrees(Math.atan2(lookDz, lookDx));
            incidentAngle = Math.toDegrees(Math.atan2(sourceDz, sourceDx)) - lookAngle;
        }
        return incidentAngle;
    }

    public static @Nullable EntityKineticBullet.EntityResult getHitResult(DamageSource damageSource) {
        Entity projectile = damageSource.getDirectEntity();
        if (projectile instanceof EntityKineticBullet bullet) {
            Vec3 startVec = bullet.position();
            Vec3 endVec = startVec.add(bullet.getDeltaMovement());
            return EntityUtil.findEntityOnPath(bullet, startVec, endVec);
        }
        return null;
    }
}
