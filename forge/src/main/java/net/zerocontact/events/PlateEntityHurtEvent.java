package net.zerocontact.events;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.HelmetInfoProvider;
import net.zerocontact.registries.ModSoundEventsReg;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlateEntityHurtEvent {
    public static boolean isHeadShot;

    public static boolean changeHurtAmountRicochet(LivingEntity lv, DamageSource source, float amount, String identifier) {
        Holder<DamageType> customDamageType = lv.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.BULLET);
        DamageSource modifiedDamageSource = new DamageSource(customDamageType);
        AtomicBoolean result = new AtomicBoolean();
        result.set(false);
        if (isHeadShot) return false;
        CuriosApi.getCuriosInventory(lv).ifPresent(iCuriosItemHandler -> iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
            ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
            float hurtAmount;
            int hurtCanHold = stack.getOrCreateTag().getInt("absorb");
            if (!(stack.isEmpty() && source.type() != modifiedDamageSource.type())) {
                lv.playSound(ModSoundEventsReg.ARMOR_HIT_PLATE);
                if (EventUtil.isDamageSourceValid(source) && stack.getItem() instanceof EntityHurtProvider provider) {
                    if (EventUtil.isIncidentAngleValid(lv, source)) {
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

    public static void entityHurtByGunHeadShot(EntityHurtByGunEvent.Pre event) {
        isHeadShot = event.isHeadShot();
        if(!isHeadShot)return;
        Optional<Entity> entity = Optional.ofNullable(event.getHurtEntity());
        DamageSource damageSource = event.getDamageSource(GunDamageSourcePart.ARMOR_PIERCING);
        entity.ifPresent(e -> {
            if (e instanceof LivingEntity livingEntity) {
                ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                Holder<DamageType> customDamageType = livingEntity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.BULLET);
                DamageSource modifiedDamageSource = new DamageSource(customDamageType);
                float amount = event.getBaseAmount();
                Optional.of(helmet).ifPresent(stack -> {
                    if (!(stack.getItem() instanceof HelmetInfoProvider && stack.getItem() instanceof EntityHurtProvider entityHurtProvider))
                        return;
                    float hurtAmount;
                    int absorb = stack.getOrCreateTag().getInt("absorb");
                    if (EventUtil.isIncidentAngleValid(livingEntity, damageSource)) {
                        hurtAmount = entityHurtProvider.generateRicochet() * amount;
                    } else if (absorb >= amount) {
                        hurtAmount = entityHurtProvider.generateBlunt() * amount;
                    } else {
                        hurtAmount = entityHurtProvider.generatePenetrated() * amount;
                    }
                    livingEntity.hurt(modifiedDamageSource, hurtAmount);
                    livingEntity.playSound(ModSoundEventsReg.HELMET_HIT);
                    event.setCanceled(true);
                });
            }
        });
    }
}
