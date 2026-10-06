package net.zerocontact.caliber.damage;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.EntityUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.events.EventUtil;
import net.zerocontact.events.ResolveHitBodyPartEvent;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPart;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPartEnum;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class HitUtil {

    public static HitPart resolveHitPart(LivingEntity target, DamageSource source, boolean headshot) {
        if (source instanceof ZDamageTypes.ZDamageSource zSource) {
            return zSource.getLastResult().hitPart();
        }
        BulletHitAccessor accessor = source.getDirectEntity() instanceof BulletHitAccessor hitAccessor ? hitAccessor : null;
        if (accessor != null) {
            HitPart cached = accessor.zc$getResolvedHitPart(target);
            if (cached != null) return cached;
        }
        EntityKineticBullet.EntityResult collision = getHitResult(source);
        Vec3 hitPosition = collision != null && collision.getEntity() == target ? collision.getHitPos() : null;
        HitPart fallback = new HitPart(headshot ? HitPartEnum.HEAD : HitPartEnum.TORSO, 1f);
        ResolveHitBodyPartEvent.Pre pre = new ResolveHitBodyPartEvent.Pre(target, source, hitPosition, fallback);
        MinecraftForge.EVENT_BUS.post(pre);
        HitPart resolved = pre.getHitPart();
        if (resolved.hitPart() == HitPartEnum.UNSET) {
            resolved = new HitPart(fallback.hitPart(), resolved.damageFactor());
        }
        if (accessor != null) accessor.zc$setResolvedHitPart(target, resolved);
        MinecraftForge.EVENT_BUS.post(new ResolveHitBodyPartEvent.Post(target, source, hitPosition, resolved));
        return resolved;
    }

    public static HitPart resolveHitPart(LivingEntity target, DamageSource source) {
        if (source instanceof ZDamageTypes.ZDamageSource zSource) {
            return zSource.getLastResult().hitPart();
        }
        EntityKineticBullet.EntityResult collision = getHitResult(source);
        return resolveHitPart(target, source,
                collision != null && collision.getEntity() == target && collision.isHeadshot());
    }

    public static ItemStack[] getHitBodyPartStack(LivingEntity target, DamageSource source, HitPart part) {
        if (part.hitPart() == HitPartEnum.HEAD) {
            return new ItemStack[]{target.getItemBySlot(EquipmentSlot.HEAD)};
        }
        if (part.isLimb()) {
            return new ItemStack[]{ItemStack.EMPTY};
        }
        return getHitBodyPartStack(target, source);
    }

    public static boolean isIncidentAngleValid(Entity lv, DamageSource source) {
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

    private static double getAngle(Entity lv, DamageSource source) {
        double incidentAngle = -361;
        if (source.getEntity() != null) {
            double sourceDx = lv.getX() - source.getEntity().getX();
            double sourceDz = lv.getZ() - source.getEntity().getZ();
            double lookDx = lv.getLookAngle().x;
            double lookDz = lv.getLookAngle().z;
            double lookAngle = Math.toDegrees(Math.atan2(lookDz, lookDx));
            incidentAngle = Mth.wrapDegrees(Math.toDegrees(Math.atan2(sourceDz, sourceDx)) - lookAngle);
        }
        return incidentAngle;
    }

    public static @Nullable EntityKineticBullet.EntityResult getHitResult(DamageSource damageSource) {
        Entity projectile = damageSource.getDirectEntity();
        if (projectile instanceof EntityKineticBullet bullet) {
            if (bullet instanceof BulletHitAccessor accessor) {
                EntityKineticBullet.EntityResult currentHit = accessor.zc$getCurrentHit();
                if (currentHit != null) return currentHit;
            }
            Vec3 startVec = bullet.position();
            Vec3 endVec = startVec.add(bullet.getDeltaMovement());
            return EntityUtil.findEntityOnPath(bullet, startVec, endVec);
        }
        return null;
    }
}
