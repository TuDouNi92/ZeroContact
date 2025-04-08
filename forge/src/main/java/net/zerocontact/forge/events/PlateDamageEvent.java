package net.zerocontact.forge.events;

import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ModLogger;
import net.zerocontact.ModSoundEvents;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class PlateDamageEvent {

    private static void DamagePlateModifier(
            ICuriosItemHandler iCuriosItemHandler,
            LivingEntity entity,
            DamageSource damageSource,
            float amount,
            String identifier
    ) {
        iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
            ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
            float durabilityLossFactor = 1;
            int hits = stack.getOrCreateTag().getInt("hits")+1;
            int durabilityLossAmount = 1;
            if (!stack.isEmpty() && damageSource.is(ModDamageTypes.BULLET)) {
                if(amount<4){
                    durabilityLossFactor=0.1f;
                }
                else if(amount<9){
                    durabilityLossFactor=0.7f;
                }
                else if(amount<13){
                    durabilityLossFactor=0.95f;
                }
                durabilityLossAmount = (int)Math.round(0.4*Math.pow(amount*durabilityLossFactor,1.5)*(1+hits*0.1f));
            }
            stack.getOrCreateTag().putInt("hits",hits);
            stack.hurtAndBreak(durabilityLossAmount, entity, lv -> {
                lv.level().playSound(null,lv.getX(),lv.getY(),lv.getZ(),ModSoundEvents.ARMOR_BROKEN_PLATE, SoundSource.PLAYERS,1.0f,1.0f);
                ModLogger.LOG.info(lv.getName() + "的插板碎掉了！");
            });
        });
    }

    public static void DamagePlateRegister(LivingEntity entity, DamageSource damageSource, float amount) {
        if (entity instanceof Player && EventUtil.isDamageSourceValid(damageSource)) {
            CuriosApi.getCuriosInventory(entity).ifPresent(inv -> {
                DamagePlateModifier(inv, entity, damageSource, amount, "front_plate");
                DamagePlateModifier(inv, entity, damageSource, amount, "back_plate");
            });
        }
    }
}
