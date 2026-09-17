package net.zerocontact.armor.modular.module.headset.client.audio;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class StaticPcmProcessor {
    private static final float SOFT_CLIP_DRIVE = 1.5F;
    private static final float SOFT_CLIP_CEILING = 0.95F;

    private StaticPcmProcessor() {
    }

    public static boolean supports(AudioFormat format) {
        return AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())
                && format.getSampleSizeInBits() == 16
                && (format.getChannels() == 1 || format.getChannels() == 2)
                && format.getFrameSize() == format.getChannels() * Short.BYTES
                && Float.isFinite(format.getSampleRate())
                && format.getSampleRate() > 0;
    }

    public static ByteBuffer copyAndProcess(ByteBuffer original, AudioFormat format, boolean process) {
        return copyAndProcess(original, format, process, true, true, true);
    }

    /** The headset preset and the two effect switches are independent. */
    public static ByteBuffer copyAndProcess(ByteBuffer original, AudioFormat format,
                                          boolean process, boolean enableTransient,
                                          boolean enableCompressor) {
        return copyAndProcess(original, format, process, enableTransient, enableCompressor, false);
    }

    /** Order: transient shaping, optional saturation, linked compression, final peak protection. */
    public static ByteBuffer copyAndProcess(ByteBuffer original, AudioFormat format,
                                          boolean process, boolean enableTransient,
                                          boolean enableCompressor, boolean enableSoftClip) {
        if (!supports(format) || original.remaining() % format.getFrameSize() != 0) {
            throw new IllegalArgumentException("Expected complete signed 16-bit PCM frames");
        }
        ByteBuffer copy = ByteBuffer.allocateDirect(original.remaining())
                .order(format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        copy.put(original.duplicate()).flip();
        AudioProcessor.TransientShaper transientShaper = null;
        if (enableTransient) {
            if (!process) {
                transientShaper = new AudioProcessor.TransientShaper(format.getSampleRate(), 12, -3);
            } else {
                transientShaper = new AudioProcessor.TransientShaper(format.getSampleRate());
            }
        }
        AudioProcessor.Compressor compressor = enableCompressor
                ? new AudioProcessor.Compressor(format.getSampleRate()) : null;
        // Keep intermediate samples as floats: saturation must precede any PCM ceiling.
        float[] samples = new float[format.getChannels()];

        // Advance the envelope once per frame; link stereo gain to preserve the stereo image.
        for (int frame = 0; frame < copy.limit(); frame += format.getFrameSize()) {
            float peak = 0;
            for (int channel = 0; channel < format.getChannels(); channel++) {
                peak = Math.max(peak, Math.abs(copy.getShort(frame + channel * Short.BYTES) / 32768.0F));
            }
            float transientGain = calculateTransientGain(peak, transientShaper);
            float compressorInputPeak = 0;
            for (int channel = 0; channel < samples.length; channel++) {
                int offset = frame + channel * Short.BYTES;
                float shaped = copy.getShort(offset) / 32768.0F * transientGain;
                samples[channel] = enableSoftClip
                        ? AudioProcessor.TransientShaper.softClip(
                                shaped, SOFT_CLIP_DRIVE, SOFT_CLIP_CEILING)
                        : shaped;
                compressorInputPeak = Math.max(compressorInputPeak, Math.abs(samples[channel]));
            }
            float compressorGain = calculateCompressorGain(compressorInputPeak, compressor);
            // Linked peak ceiling prevents boosted transients wrapping signed 16-bit PCM.
            if ((enableTransient || enableCompressor || enableSoftClip) && compressorInputPeak > 0) {
                compressorGain = Math.min(compressorGain, (32767.0F / 32768.0F) / compressorInputPeak);
            }
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int offset = frame + channel * Short.BYTES;
                copy.putShort(offset, (short) Math.round(samples[channel] * compressorGain * 32768.0F));
            }
        }
        return copy;
    }

    private static float calculateTransientGain(float peak,
                                               AudioProcessor.TransientShaper shaper) {
        return shaper == null ? 1.0F : shaper.processGain(peak);
    }

    private static float calculateCompressorGain(float peak,
                                                AudioProcessor.Compressor compressor) {
        if (compressor == null) {
            return 1.0F;
        }
        // Still advance the release envelope during silence.
        float compressed = compressor.process(peak);
        return peak == 0 ? 1.0F : compressed / peak;
    }
}
