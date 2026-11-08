package net.zerocontact.armor.modular.module.radio.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.INBTSerializable;
import net.zerocontact.armor.modular.module.radio.model.RadioProfile;
import net.zerocontact.armor.modular.module.radio.model.RadioState;
import net.zerocontact.armor.modular.module.radio.model.RadioTransmission;

public interface MBITR extends INBTSerializable<CompoundTag> {

    RadioState getRadioState();

    RadioProfile getProfile();

    void setRadioActivated(boolean radioActivated);

    void setProfile(RadioProfile profile);

    boolean transmitting();

    boolean canReceiveFrom(RadioTransmission transmission);

    void scanChannel(ServerPlayer player);

    void talk();

    void endTalk();

}
