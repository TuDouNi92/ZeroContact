package net.zerocontact.forge.events;

import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.ModLogger;
public class EventUtil {

    //判断伤害源是否合法
    public static boolean isDamageSourceValid(DamageSource damageSource){
        return damageSource.is(DamageTypes.ARROW)
                || damageSource.is(DamageTypes.PLAYER_ATTACK)
                || damageSource.is(DamageTypes.MOB_ATTACK)
                || damageSource.is(DamageTypes.MOB_PROJECTILE)
                || damageSource.is(ModDamageTypes.BULLET)
                || damageSource.is(DamageTypes.EXPLOSION)
                || damageSource.is(DamageTypes.PLAYER_EXPLOSION);
    }

    //判断入射角是否合法，服务于判断跳弹功能
    public static boolean isIncidentAngleValid(LivingEntity lv, DamageSource source, float amount){
        double sourceDx = lv.getX() - source.getEntity().getX();
        double sourceDz = lv.getZ() - source.getEntity().getZ();
        double lookDx = lv.getLookAngle().x;
        double lookDz = lv.getLookAngle().z;
        double lookAngle = Math.toDegrees(Math.atan2(lookDz,lookDx));
        double incidentAngle = Math.toDegrees(Math.atan2(sourceDz,sourceDx)) -lookAngle;
        if(incidentAngle<-180){
            incidentAngle+=360;
        }
        else if(incidentAngle>180){
            incidentAngle -=360;
        }
        ModLogger.LOG.info(incidentAngle);
        if((Math.abs(Math.abs(incidentAngle)-90)<30)&&(Math.abs(Math.abs(incidentAngle)-90)>=10)){
            ModLogger.LOG.info("跳弹！");
            return true;
        }
        return false;
    }
}
