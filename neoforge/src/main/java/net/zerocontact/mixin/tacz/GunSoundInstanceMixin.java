package net.zerocontact.mixin.tacz;

import com.tacz.guns.client.sound.GunSoundInstance;
import net.zerocontact.armor.modular.module.headset.client.audio.AudioBehaviorManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GunSoundInstance.class, remap = false)
public class GunSoundInstanceMixin {
    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;min(FF)F"
            ),
            index = 0
    )
    private float modifyMinuend(float minuend) {
        return AudioBehaviorManager.getAudioBehaviorCalc().overrideGunSoundMinuend();
    }
}
