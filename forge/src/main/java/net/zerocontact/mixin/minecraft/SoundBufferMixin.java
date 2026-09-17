package net.zerocontact.mixin.minecraft;

import com.mojang.blaze3d.audio.SoundBuffer;
import net.zerocontact.armor.modular.module.headset.client.audio.StaticPcmProcessor;
import net.zerocontact.armor.modular.module.headset.client.audio.StaticSoundBufferSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.OptionalInt;

@Mixin(SoundBuffer.class)
public abstract class SoundBufferMixin implements StaticSoundBufferSource {
    @Shadow
    private ByteBuffer data;
    @Shadow
    @Final
    private AudioFormat format;

    @Unique
    private ByteBuffer zeroContact$originalPcm;
    @Unique
    private SoundBuffer zeroContact$processedActivatedBuffer;

    @Unique
    private SoundBuffer zeroContact$processedDeactivatedBuffer;

    @Unique
    private void zeroContact$retainOriginalPcm() {
        if (zeroContact$originalPcm == null && data != null && StaticPcmProcessor.supports(format)) {
            zeroContact$originalPcm = data.asReadOnlyBuffer();
        }
    }

    @Inject(method = "getAlBuffer", at = @At("HEAD"))
    private void zeroContact$retainBeforeUpload(CallbackInfoReturnable<OptionalInt> cir) {
        // An excluded sound can upload a shared buffer before a gun sound uses it.
        zeroContact$retainOriginalPcm();
    }
    @Override
    public SoundBuffer zeroContact$forPlayback(boolean process) {
        SoundBuffer original = (SoundBuffer) (Object) this;
        if (!StaticPcmProcessor.supports(format)) {
            return original;
        }
        zeroContact$retainOriginalPcm();
        if (zeroContact$originalPcm == null
                || zeroContact$originalPcm.remaining() % format.getFrameSize() != 0) {
            return original;
        }
        // Static channels may share this buffer: playback state belongs to the channel.
        // Shaping/compression parameters are fixed, so processing the same PCM again is unnecessary.


        if (process) {
            if (zeroContact$processedActivatedBuffer == null) {
                zeroContact$processedActivatedBuffer = new SoundBuffer(
                        StaticPcmProcessor.copyAndProcess(zeroContact$originalPcm, format, process,true,true,false), format);
            }
        } else {
            if (zeroContact$processedDeactivatedBuffer == null) {
                zeroContact$processedDeactivatedBuffer = new SoundBuffer(
                        StaticPcmProcessor.copyAndProcess(zeroContact$originalPcm, format, process), format);
            }
        }

        return process ? zeroContact$processedActivatedBuffer : zeroContact$processedDeactivatedBuffer;
    }

    @Inject(method = "discardAlBuffer", at = @At("RETURN"))
    private void zeroContact$discardProcessedBuffer(CallbackInfo ci) {
        // Follow the original cache's lifecycle, not an individual channel's lifetime.
        // Also runs when only the processed version was uploaded to OpenAL.
        if (zeroContact$processedActivatedBuffer != null) {
            zeroContact$processedActivatedBuffer.discardAlBuffer();
            zeroContact$processedActivatedBuffer = null;
        }

        if (zeroContact$processedDeactivatedBuffer != null) {
            zeroContact$processedDeactivatedBuffer.discardAlBuffer();
            zeroContact$processedDeactivatedBuffer = null;
        }


        zeroContact$originalPcm = null;
    }
}
