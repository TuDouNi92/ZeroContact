package net.zerocontact.armor.modular.module.beacon.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.zerocontact.ZeroContact;

import java.util.Arrays;

public class BeaconContainer implements INBTSerializable<CompoundTag> {
    public static final ResourceLocation STATE_ON = new ResourceLocation(ZeroContact.MOD_ID, "beacon/on");
    public static final ResourceLocation STATE_RED = new ResourceLocation(ZeroContact.MOD_ID, "beacon/red");
    public static final ResourceLocation STATE_GREEN = new ResourceLocation(ZeroContact.MOD_ID, "beacon/green");
    public static final ResourceLocation STATE_BLUE = new ResourceLocation(ZeroContact.MOD_ID, "beacon/blue");
    public static final ResourceLocation STATE_OFF = new ResourceLocation(ZeroContact.MOD_ID, "beacon/off");
    public static final String NBT_MODE = "mode";

    private Mode mode = Mode.OFF;

    public enum Mode {
        OFF,
        RED,
        GREEN,
        BLUE
    }


    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean getLightOn() {
        return mode != Mode.OFF;
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(NBT_MODE, mode.name());
        return tag;
    }


    @Override
    public void deserializeNBT(CompoundTag arg) {
        mode = Arrays.stream(Mode.values()).filter(mode -> mode.name().equals(arg.getString(NBT_MODE))).findAny().orElse(Mode.OFF);
    }
}
