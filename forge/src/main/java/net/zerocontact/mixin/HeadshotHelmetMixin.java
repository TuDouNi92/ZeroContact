package net.zerocontact.mixin;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.zerocontact.events.EventUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class HeadshotHelmetMixin {
    @Unique
    private static final int[] BODY_SLOTS = new int[]{
            EquipmentSlot.FEET.getIndex(),
            EquipmentSlot.LEGS.getIndex(),
            EquipmentSlot.CHEST.getIndex()
    };
    @Shadow
    @Final
    private Inventory inventory;
    @Inject(method = "hurtArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;hurtArmor(Lnet/minecraft/world/damagesource/DamageSource;F[I)V"), cancellable = true)
    public void zeroContact$hurtHelmet(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        if (damageSource.is(ModDamageTypes.BULLETS_TAG)) {
            EntityKineticBullet.EntityResult result = EventUtil.getHitResult(damageSource);
            if (result != null && result.isHeadshot()) {
                ci.cancel();
                this.inventory.hurtArmor(damageSource, damageAmount, Inventory.HELMET_SLOT_ONLY);
            } else {
                ci.cancel();
                this.inventory.hurtArmor(damageSource, damageAmount, BODY_SLOTS);
            }
        }
    }
}
