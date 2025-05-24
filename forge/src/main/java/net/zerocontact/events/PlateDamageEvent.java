package net.zerocontact.events;

import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ModLogger;
import net.zerocontact.api.DurabilityLossProvider;
import net.zerocontact.registries.ModSoundEventsReg;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class PlateDamageEvent {

    private static void DamagePlateModifier(
            ICuriosItemHandler iCuriosItemHandler,
            LivingEntity livingEntity,
            DamageSource damageSource,
            float amount,
            String identifier
    ) {
        iCuriosItemHandler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
            ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
            float durabilityLossFactor = 1;
            int hits = stack.getOrCreateTag().getInt("hits") + 1;
            int durabilityLossAmount = 1;
            if (!stack.isEmpty() && (damageSource.is(ModDamageTypes.BULLET) || damageSource.is(ModDamageTypes.BULLET_IGNORE_ARMOR))) {
                switch (ProtectionLevelHelper.get((int)Math.floor(amount))){
                    case NIJIIA -> durabilityLossFactor = 0.1F;
                    case NIJII -> durabilityLossFactor = 0.4F;
                    case NIJIIIA -> durabilityLossFactor = 0.7F;
                    case NIJIII -> durabilityLossFactor = 1.1F;
                    case NIJIV -> durabilityLossFactor = 1.5F;
                }
                if(stack.getItem() instanceof DurabilityLossProvider provider){
                    durabilityLossAmount = provider.generateLoss(amount,durabilityLossFactor,hits);
                }
            }
            stack.getOrCreateTag().putInt("hits", hits);
            stack.hurtAndBreak(durabilityLossAmount, livingEntity, lv -> {
                lv.level().playSound(null, lv.getX(), lv.getY(), lv.getZ(), ModSoundEventsReg.ARMOR_BROKEN_PLATE, SoundSource.PLAYERS, 1.0f, 1.0f);
                ModLogger.LOG.info(lv.getName() + "的插板碎掉了！");
            });
        });
    }

    public static void DamagePlateRegister(LivingEntity entity, DamageSource damageSource, float amount) {
        if (entity instanceof Player && EventUtil.isDamageSourceValid(damageSource)) {
            CuriosApi.getCuriosInventory(entity).ifPresent(inv -> {
                DamagePlateModifier(inv, entity, damageSource, amount, EventUtil.idHitFromBack(entity, damageSource));
            });
        }
    }
}
