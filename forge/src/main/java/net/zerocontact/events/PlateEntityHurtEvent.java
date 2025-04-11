package net.zerocontact.events;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ModLogger;
import net.zerocontact.item.SapiIV;
import net.zerocontact.registries.ModSoundEventsReg;
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
                ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
                if (!(stack.isEmpty() && source.type() != modifiedDamageSource.type())) {
                    lv.playSound(ModSoundEventsReg.ARMOR_HIT_PLATE);

                    if (lv instanceof Player && EventUtil.isDamageSourceValid(source)
                    ) {
                        ModLogger.LOG.info(source);

                        if (EventUtil.isIncidentAngleValid(lv, source, amount)) {
                            float hurtAmount = amount * 0.05f;
                            lv.hurt(modifiedDamageSource, hurtAmount);
                            ModLogger.LOG.info("跳弹伤害{}", hurtAmount);
                            result.set(true);
                        } else {
                            result.set(false);
                        }

                        float hurtAmount;
                        if (SapiIV.MAX_HURT_DAMAGE_CAN_HOLD > amount) {
                            //钝伤
                            hurtAmount = amount * 0.1f;
                            ModLogger.LOG.info("blunt hurt:{}", hurtAmount);
                        } else {
                            //贯穿
                            hurtAmount = amount * 0.7f;
                            ModLogger.LOG.info("penetrate hurt:{}", hurtAmount);
                        }
                        lv.hurt(modifiedDamageSource, hurtAmount);
                        result.set(true);
                    }
                    result.set(false);
                }
            });
        });
        return result.get();
    }
}
