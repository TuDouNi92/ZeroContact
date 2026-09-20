package net.zerocontact.mixin.minecraft;

import com.mojang.blaze3d.audio.SoundBuffer;
import net.zerocontact.armor.modular.module.headset.client.audio.StaticPcmProcessor;
import net.zerocontact.armor.modular.module.headset.client.audio.StaticSoundBufferSource;
import net.zerocontact.armor.modular.module.headset.item.Headset;
import org.jetbrains.annotations.Nullable;
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
import java.util.HashMap;
import java.util.Map;

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
    private final Map<Headset.AudioProfile, SoundBuffer> zeroContact$processedActivatedBuffers = new HashMap<>();

    @Unique
    private final Map<Headset.AudioProfile, SoundBuffer> zeroContact$processedDeactivatedBuffers = new HashMap<>();

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
    public SoundBuffer zeroContact$forPlayback(boolean process, @Nullable Headset.AudioProfile profile) {
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
        // EQ parameters are supplied by the immutable profile; do not reuse another profile's PCM.
        Map<Headset.AudioProfile, SoundBuffer> buffers = process
                ? zeroContact$processedActivatedBuffers : zeroContact$processedDeactivatedBuffers;
        SoundBuffer processed = buffers.get(profile);
        if (processed == null) {
            processed = new SoundBuffer(
                    StaticPcmProcessor.copyAndProcess(zeroContact$originalPcm, format, process, profile), format);
            buffers.put(profile, processed);
        }
        return processed;
    }

    @Inject(method = "discardAlBuffer", at = @At("RETURN"))
    private void zeroContact$discardProcessedBuffer(CallbackInfo ci) {
        // Follow the original cache's lifecycle, not an individual channel's lifetime.
        // Also runs when only the processed version was uploaded to OpenAL.
        for (SoundBuffer buffer : zeroContact$processedActivatedBuffers.values()) {
            buffer.discardAlBuffer();
        }
        zeroContact$processedActivatedBuffers.clear();
        for (SoundBuffer buffer : zeroContact$processedDeactivatedBuffers.values()) {
            buffer.discardAlBuffer();
        }
        zeroContact$processedDeactivatedBuffers.clear();
        zeroContact$originalPcm = null;
    }
}
