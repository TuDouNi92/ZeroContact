package net.zerocontact.armor.modular.module.radio.container;

import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.module.radio.api.MBITR;
import net.zerocontact.armor.modular.module.radio.model.RadioProfile;
import net.zerocontact.armor.modular.module.radio.model.RadioState;
import net.zerocontact.armor.modular.module.radio.model.RadioTransmission;
import net.zerocontact.capability.CapabilityRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RadioContainer implements MBITR {
    private static final int holdLastScanTimedOut = 40;
    private static final double RANGE = 16 * 8;
    public static final String NBT_ACTIVATED = "activated";
    public static final String NBT_PROFILE_CHANNEL = "profile_channel";
    public static final String NBT_SUB_CHANNEL = "sub_channel";
    private boolean radioActivated = false;
    private boolean transmitting;
    private @Nullable Vec3 radioPos;

    private RadioProfile currentProfile = new RadioProfile(
            0,
            List.of()
    );

    private int currentChannel = currentProfile.homeChannel();

    private int capturedChannel = 0;


    public void setRadioActivated(boolean radioActivated) {
        this.radioActivated = radioActivated;
        if (!radioActivated) {
            endTalk();
            radioPos = null;
            capturedChannel = 0;
        }
    }


    @Override
    public RadioState getRadioState() {
        return new RadioState(
                radioActivated,
                currentChannel
        );
    }

    @Override
    public RadioProfile getProfile() {
        return currentProfile;
    }

    @Override
    public void setProfile(RadioProfile profile) {
        this.currentProfile = profile;
        if (!transmitting) {
            currentChannel = profile.homeChannel();
        }
        capturedChannel = 0;
    }

    @Override
    public boolean transmitting() {
        return transmitting && radioActivated;
    }

    @Override
    public boolean canReceiveFrom(RadioTransmission transmission) {
        if (!radioActivated || radioPos == null) return false;
        if (!transmission.radioState().radioActivated()) return false;
        if (transmission.senderPos().distanceToSqr(radioPos) <= RANGE * RANGE) {
            int senderChannel = transmission.radioState().currentChannel();
            return currentChannel == senderChannel || currentProfile.subChannels().contains(senderChannel);
        }
        return false;
    }

    @Override
    public void scanChannel(ServerPlayer player) {
        if (!radioActivated) {
            radioPos = null;
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        radioPos = player.position();
        if (!transmitting) {
            if (player.tickCount % holdLastScanTimedOut == 0) {
                capturedChannel = 0;
            }
            List<Integer> activateChannels = new ArrayList<>();
            level.getPlayers(other -> {
                if (radioPos != null) {
                    return !other.getUUID().equals(player.getUUID()) && other.distanceToSqr(radioPos) <= RANGE * RANGE;
                }
                return false;
            }).forEach(other -> {
                ModuleQuery.streamMounted(other).forEach(ref -> {
                    ref.stack().getCapability(CapabilityRegistries.RADIO).ifPresent(otherRadio -> {
                        if (otherRadio.transmitting()) {
                            currentProfile.subChannels()
                                    .stream()
                                    .filter(c -> c.equals(otherRadio.getRadioState().currentChannel()))
                                    .findFirst()
                                    .ifPresent(activateChannels::add);
                        }
                    });
                });
            });
            if (activateChannels.isEmpty()) return;
            Set<Integer> activateSet = new LinkedHashSet<>(activateChannels);
            capturedChannel = activateSet.stream().toList().get(activateSet.size() - 1);
        }
    }


    @Override
    public void talk() {
        transmitting = true;
        if (capturedChannel != 0.0f) {
            currentChannel = capturedChannel;
        } else {
            currentChannel = currentProfile.homeChannel();
        }
    }

    @Override
    public void endTalk() {
        transmitting = false;
        currentChannel = currentProfile.homeChannel();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(NBT_ACTIVATED, radioActivated);
        tag.putInt(NBT_PROFILE_CHANNEL, currentProfile.homeChannel());
        ListTag listTag = new ListTag();
        for (Integer channel : currentProfile.subChannels()) {
            listTag.add(IntTag.valueOf(channel));
        }
        tag.put(NBT_SUB_CHANNEL, listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        radioActivated = arg.getBoolean(NBT_ACTIVATED);
        ListTag listTag = arg.getList(NBT_SUB_CHANNEL, Tag.TAG_INT);
        List<Integer> list = new ArrayList<>();
        listTag.forEach(tag -> {
            list.add(((IntTag) tag).getAsInt());
        });
        currentProfile = new RadioProfile(
                arg.getInt(NBT_PROFILE_CHANNEL),
                list
        );
        currentChannel = currentProfile.homeChannel();
        capturedChannel = 0;
        transmitting = false;
    }
}
