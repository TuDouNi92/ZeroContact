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

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

@Mixin(SoundBuffer.class)
public abstract class SoundBufferMixin implements StaticSoundBufferSource {
    @Shadow private ByteBuffer data;
    @Shadow @Final private AudioFormat format;

    @Unique private ByteBuffer zeroContact$originalPcm;
    @Unique private SoundBuffer zeroContact$processedBuffer;

    @Override
    public SoundBuffer zeroContact$forPlayback(boolean process) {
        SoundBuffer original = (SoundBuffer) (Object) this;
        if (!StaticPcmProcessor.supports(format)) {
            return original;
        }
        // Called only by static attachment, before getAlBuffer() clears data.
        // Retain it even without a headset, so later playback can still be processed.
        if (zeroContact$originalPcm == null && data != null) {
            zeroContact$originalPcm = data.asReadOnlyBuffer();
        }
        if (!process || zeroContact$originalPcm == null
                || zeroContact$originalPcm.remaining() % format.getFrameSize() != 0) {
            return original;
        }
        // Static channels may share this buffer: playback state belongs to the channel.
        // Compression parameters are fixed, so processing the same PCM again is unnecessary.
        if (zeroContact$processedBuffer == null) {
            zeroContact$processedBuffer = new SoundBuffer(
                    StaticPcmProcessor.copyAndProcess(zeroContact$originalPcm, format), format);
        }
        return zeroContact$processedBuffer;
    }

    @Inject(method = "discardAlBuffer", at = @At("RETURN"))
    private void zeroContact$discardProcessedBuffer(CallbackInfo ci) {
        // Follow the original cache's lifecycle, not an individual channel's lifetime.
        // Also runs when only the processed version was uploaded to OpenAL.
        if (zeroContact$processedBuffer != null) {
            zeroContact$processedBuffer.discardAlBuffer();
            zeroContact$processedBuffer = null;
        }
        zeroContact$originalPcm = null;
    }
}
