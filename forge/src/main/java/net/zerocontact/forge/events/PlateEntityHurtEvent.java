package net.zerocontact.forge.events;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ModLogger;
import net.zerocontact.ModSoundEvents;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class PlateEntityHurtEvent {

    private static void DefenseModifier(
            LivingEntity entity,
            DamageSource damageSource,
            float amount,
            String identifier,
            ICuriosItemHandler iCuriosItemHandler
    ) {
        iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
            ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
            if (entity instanceof Player && EventUtil.isDamageSourceValid(damageSource)) {

                if (damageSource.getDirectEntity() instanceof Player) {

                } else {

                }
            }
        });
    }

    //切记啊这里曾经有个source递归调用
    public static boolean changeHurtAmount(LivingEntity lv, DamageSource source, float amount, String identifier) {
        Holder<DamageType> customDamageType = lv.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC);
        DamageSource modifiedDamageSource = new DamageSource(customDamageType);
        AtomicBoolean result = new AtomicBoolean();
        result.set(false);
        CuriosApi.getCuriosInventory(lv).ifPresent(iCuriosItemHandler -> {
            iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
                if (!(stacksHandler.getStacks().getStackInSlot(0).isEmpty())) {
                    lv.playSound(ModSoundEvents.ARMOR_HIT_PLATE);
                    if (!(source.type() == modifiedDamageSource.type())) {
                        if (lv instanceof Player && EventUtil.isDamageSourceValid(source)
                        ) {
                            ModLogger.LOG.info(source);
                            if(EventUtil.isIncidentAngleValid(lv, source, amount)){
                                lv.hurt(modifiedDamageSource, amount / 3);
                                result.set(true);
                            }
                            else{
                                result.set(false);
                            }
                            if (amount <= 3) {
                                lv.hurt(modifiedDamageSource, amount / 2);
                                result.set(true);
                            } else {
                                result.set(false);
                            }

                        }
                    }
                }
            });
        });
        return result.get();
    }
}
