package net.zerocontact.mixin.minecraft;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.zerocontact.armor.modular.module.headset.client.audio.AudioBehaviorManager;
import net.zerocontact.armor.modular.module.headset.client.audio.HeadsetAudioState;
import net.zerocontact.armor.modular.module.headset.client.audio.StaticSoundBufferSource;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    // Forge 1.20.1's inner playback callback runs on the sound thread, before attach/play.
    @Dynamic("Synthetic static-buffer playback callback added by Forge 1.20.1")
    // javac hides synthetic methods from the annotation processor; verified in the runtime class.
    @SuppressWarnings("target")
    @ModifyVariable(
            method = "lambda$play$6(Lcom/mojang/blaze3d/audio/SoundBuffer;Lnet/minecraft/client/resources/sounds/SoundInstance;Lcom/mojang/blaze3d/audio/Channel;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 1,
            remap = false
    )
    private SoundBuffer zeroContact$processStaticBuffer(SoundBuffer original, SoundBuffer arg,
                                                        SoundInstance soundinstance, Channel arg2) {
        return AudioBehaviorManager.getAudioBehaviorCalc().shouldAttachToPCM(soundinstance)
                ? ((StaticSoundBufferSource) original).zeroContact$forPlayback(HeadsetAudioState.isActive())
                : original;
    }
}
