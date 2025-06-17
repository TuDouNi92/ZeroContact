package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.registries.ModSoundEventsReg;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;

public class PlateEntityHurtEvent {
    public static boolean isHeadShot;
    public static boolean changeHurtAmountRicochet(LivingEntity lv, DamageSource source, float amount, String identifier) {
        Holder<DamageType> customDamageType = lv.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC);
        DamageSource modifiedDamageSource = new DamageSource(customDamageType);
        AtomicBoolean result = new AtomicBoolean();
        result.set(false);
        CuriosApi.getCuriosInventory(lv).ifPresent(iCuriosItemHandler -> iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
            ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
            float hurtAmount;
            int hurtCanHold = stack.getOrCreateTag().getInt("absorb");
            if (!(stack.isEmpty() && source.type() != modifiedDamageSource.type())) {
                if(isHeadShot)return;
                lv.playSound(ModSoundEventsReg.ARMOR_HIT_PLATE);
                if (EventUtil.isDamageSourceValid(source) && stack.getItem() instanceof EntityHurtProvider provider) {
                    if (EventUtil.isIncidentAngleValid(lv, source, amount)) {
                        hurtAmount = provider.generateRicochet() * amount;
                        lv.hurt(modifiedDamageSource, hurtAmount);
                    } else {
                        if (hurtCanHold >= amount) {
                            //钝伤
                            hurtAmount = provider.generateBlunt() * amount;

                        } else {
                            //贯穿
                            hurtAmount = provider.generatePenetrated() * amount;

                        }
                    }
                    lv.hurt(modifiedDamageSource, hurtAmount);
                    result.set(true);
                } else {
                    result.set(false);
                }
            }
        }));
        return result.get();
    }
    public static void entityHurtByGunHeadShot(EntityHurtByGunEvent event){
        isHeadShot = event.isHeadShot();
    }
}
