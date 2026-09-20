package net.zerocontact.armor.modular.module.headset.client.audio;

import com.mojang.blaze3d.audio.SoundBuffer;
import net.zerocontact.armor.modular.module.headset.item.Headset;
import org.jetbrains.annotations.Nullable;

public interface StaticSoundBufferSource {
    SoundBuffer zeroContact$forPlayback(boolean process, @Nullable Headset.AudioProfile profile);
}
