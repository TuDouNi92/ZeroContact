package net.zerocontact.events;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.EntityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.api.IEquipmentTypeTag;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicReference;

public class EventUtil {

    //判断入射角是否合法，服务于判断跳弹功能
    public static boolean isIncidentAngleValid(LivingEntity lv, DamageSource source) {
        double incidentAngle = getAngle(lv, source);
        double incidentAngleAbs = Math.abs(incidentAngle);
        if (incidentAngle != -361) {
            return (Math.abs(incidentAngleAbs - 90) <= 30) && (Math.abs(incidentAngleAbs - 90) >= 10);
        }
        return false;
    }

    public static ItemStack getHitBodyPartStack(LivingEntity lv, DamageSource source) {
        double incidentAngleAbs = Math.abs(getAngle(lv, source));
        AtomicReference<ItemStack> defenseStack = new AtomicReference<>(ItemStack.EMPTY);
        if (lv.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof IEquipmentTypeTag tag && tag.getArmorType().equals(IEquipmentTypeTag.EquipmentType.ARMOR)) {
            defenseStack.set(lv.getItemBySlot(EquipmentSlot.CHEST));
            return defenseStack.get();
        }
        CuriosApi.getCuriosInventory(lv).ifPresent(iCuriosItemHandler -> {
            if (incidentAngleAbs != 361) {
                if (incidentAngleAbs > 90) {
                    iCuriosItemHandler.getStacksHandler("front_plate").ifPresent(stacksHandler -> defenseStack.set(stacksHandler.getStacks().getStackInSlot(0)));
                } else {
                    iCuriosItemHandler.getStacksHandler("back_plate").ifPresent(stacksHandler -> defenseStack.set(stacksHandler.getStacks().getStackInSlot(0)));
                }
            }
        });
        return defenseStack.get();
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

    public static ServerPlayer getAllyPlayer(Player player) {
        double range = 1.5D;
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 reachVec = eyePos.add(lookVec.scale(range));
        AABB box = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(1.0D);
        EntityHitResult result = ProjectileUtil.getEntityHitResult(player.level(), player, eyePos, reachVec, box, e -> e instanceof ServerPlayer sp && !sp.isSpectator() && sp.isPickable() && sp.isAlliedTo(player));
        return result != null ? (ServerPlayer) result.getEntity() : null;
    }

    public static boolean isLookAtTargetBack(ServerPlayer player, @Nullable LivingEntity target) {
        if (target == null) return false;
        Vec3 look = player.position().subtract(target.position()).normalize();
        float yRot = target.getYRot();
        Vec3 targetForward = Vec3.directionFromRotation(0, yRot).normalize();
        Vec3 targetBack = targetForward.scale(-1);
        double dot = look.dot(targetBack);
        return dot > 0;
    }

    public static ItemStack getCuriosStackFirst(LivingEntity player, String id) {
        ItemStack[] stacks = {null};
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.getStacksHandler(id).ifPresent(iCurioStacksHandler -> {
                ItemStack stack = iCurioStacksHandler.getStacks().getStackInSlot(0);
                stacks[0] = stack;
            });
        });
        return stacks[0];
    }
}
