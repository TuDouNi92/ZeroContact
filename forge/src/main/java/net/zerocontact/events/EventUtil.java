package net.zerocontact.events;

import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.ModLogger;

public class EventUtil {

    //判断伤害源是否合法
    public static boolean isDamageSourceValid(DamageSource damageSource) {
        return damageSource.is(DamageTypes.ARROW)
                || damageSource.is(DamageTypes.PLAYER_ATTACK)
                || damageSource.is(DamageTypes.MOB_PROJECTILE)
                || damageSource.is(ModDamageTypes.BULLET)
                || damageSource.is(DamageTypes.EXPLOSION)
                || damageSource.is(DamageTypes.PLAYER_EXPLOSION);
    }

    //判断入射角是否合法，服务于判断跳弹功能
    public static boolean isIncidentAngleValid(LivingEntity lv, DamageSource source, float amount) {
        double incidentAngle = getAngle(lv, source);
        double incidentAngleAbs = Math.abs(incidentAngle);
        if(incidentAngle!=-361){
            if (incidentAngle < -180) {
                incidentAngle += 360;
            } else if (incidentAngle > 180) {
                incidentAngle -= 360;
            }
            ModLogger.LOG.info(incidentAngle);
            if ((Math.abs(incidentAngleAbs - 90) < 30) && (Math.abs(incidentAngleAbs - 90) >= 10)) {
                ModLogger.LOG.info("跳弹！");
                return true;
            }
        }
        return false;
    }

    public static String idHitFromBack(LivingEntity lv, DamageSource source) {
        double incidentAngleAbs = Math.abs(getAngle(lv, source));
        if(incidentAngleAbs !=361){
            if (incidentAngleAbs > 90 ) {
                ModLogger.LOG.info("正面着弹");
                return "front_plate";
            } else {
                ModLogger.LOG.info("反面着弹");
                return "back_plate";
            }
        }
        return "";
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
}
