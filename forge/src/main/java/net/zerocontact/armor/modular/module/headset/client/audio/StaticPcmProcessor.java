package net.zerocontact.armor.modular.module.headset.client.audio;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class StaticPcmProcessor {
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

    public static ByteBuffer copyAndProcess(ByteBuffer original, AudioFormat format) {
        if (!supports(format) || original.remaining() % format.getFrameSize() != 0) {
            throw new IllegalArgumentException("Expected complete signed 16-bit PCM frames");
        }
        ByteBuffer copy = ByteBuffer.allocateDirect(original.remaining())
                .order(format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        copy.put(original.duplicate()).flip();
        ClientSoundEngineHandler.Compressor compressor =
                new ClientSoundEngineHandler.Compressor(format.getSampleRate());

        // Advance the envelope once per frame; link stereo gain to preserve the stereo image.
        for (int frame = 0; frame < copy.limit(); frame += format.getFrameSize()) {
            float peak = 0;
            for (int channel = 0; channel < format.getChannels(); channel++) {
                peak = Math.max(peak, Math.abs(copy.getShort(frame + channel * Short.BYTES) / 32768.0F));
            }
            float compressed = compressor.process(peak);
            float gain = peak == 0 ? 1 : compressed / peak;
            for (int channel = 0; channel < format.getChannels(); channel++) {
                int offset = frame + channel * Short.BYTES;
                copy.putShort(offset, (short) Math.round(copy.getShort(offset) * gain));
            }
        }
        return copy;
    }
}
