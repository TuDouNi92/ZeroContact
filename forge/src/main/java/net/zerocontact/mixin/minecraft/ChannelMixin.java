package net.zerocontact.mixin.minecraft;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.zerocontact.armor.modular.module.headset.client.audio.ClientHeadsetState;
import net.zerocontact.armor.modular.module.headset.client.audio.StaticSoundBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Channel.class)
public class ChannelMixin {
    @ModifyVariable(method = "attachStaticBuffer", at = @At("HEAD"), argsOnly = true)
    private SoundBuffer zeroContact$processStaticBuffer(SoundBuffer original) {
        return ((StaticSoundBufferSource) original)
                .zeroContact$forPlayback(ClientHeadsetState.isActive());
    }

    @ModifyVariable(
            method = "linearAttenuation",
            at = @At("HEAD"),
            argsOnly = true
    )
    public float linearAttenuation(float originalAttenuation) {
        if (!ClientHeadsetState.isActive()) {
            return originalAttenuation;
        }
        return originalAttenuation * 2;
    }

}
