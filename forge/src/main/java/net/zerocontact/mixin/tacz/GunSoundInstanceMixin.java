package net.zerocontact.mixin.tacz;

import com.tacz.guns.client.sound.GunSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GunSoundInstance.class, remap = false)
public class GunSoundInstanceMixin {
    @ModifyArg(
            method = "<init>(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFLnet/minecraft/world/entity/Entity;ILnet/minecraft/resources/ResourceLocation;ZZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;min(FF)F"
            ),
            index = 0
    )
    private float modifyMinuend(float minuend) {
        return 0.8175f;
    }
}
