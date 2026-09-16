package net.zerocontact.armor.modular.module.headset.client.audio;

import com.mojang.blaze3d.audio.SoundBuffer;

public interface StaticSoundBufferSource {
    SoundBuffer zeroContact$forPlayback(boolean process);
}
