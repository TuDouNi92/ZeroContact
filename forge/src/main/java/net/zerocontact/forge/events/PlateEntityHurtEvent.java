package net.zerocontact.forge.events;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zerocontact.ModLogger;
import net.zerocontact.ModSoundEvents;
import net.zerocontact.SapiIV;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;

public class PlateEntityHurtEvent {

    public static boolean changeHurtAmountRicochet(LivingEntity lv, DamageSource source, float amount, String identifier) {
        Holder<DamageType> customDamageType = lv.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC);
        DamageSource modifiedDamageSource = new DamageSource(customDamageType);
        AtomicBoolean result = new AtomicBoolean();

        result.set(false);
        CuriosApi.getCuriosInventory(lv).ifPresent(iCuriosItemHandler -> {
            iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
                if (!(stacksHandler.getStacks().getStackInSlot(0).isEmpty() && source.type() != modifiedDamageSource.type())) {
                    lv.playSound(ModSoundEvents.ARMOR_HIT_PLATE);

                    if (lv instanceof Player && EventUtil.isDamageSourceValid(source)
                    ) {
                        ModLogger.LOG.info(source);

                        if (EventUtil.isIncidentAngleValid(lv, source, amount)) {
                            float hurtAmount = amount*0.1f;
                            lv.hurt(modifiedDamageSource, hurtAmount);
                            ModLogger.LOG.info(hurtAmount);
                            result.set(true);
                        } else {
                            result.set(false);
                        }

                        if (SapiIV.MAX_HURT_DAMAGE_CAN_HOLD > amount) {
                            //钝伤
                            float hurtAmount = amount*0.2f;
                            lv.hurt(modifiedDamageSource,hurtAmount);
                            ModLogger.LOG.info(hurtAmount);
                            result.set(true);
                        } else {
                            //贯穿
                            float hurtAmount = amount*0.6f;
                            lv.hurt(modifiedDamageSource,hurtAmount);
                            ModLogger.LOG.info(hurtAmount);
                            result.set(true);
                        }
                    }

                }
            });
        });
        return result.get();
    }
}
