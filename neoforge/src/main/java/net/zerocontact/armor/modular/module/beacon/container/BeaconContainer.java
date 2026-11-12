package net.zerocontact.armor.modular.module.beacon.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.container.StackModuleState;

import java.util.Arrays;

public class BeaconContainer extends StackModuleState {
    public BeaconContainer() {
        super("beacon");
    }

    public BeaconContainer(ItemStack stack) {
        this();
        bind(stack);
    }

    public static final ResourceLocation STATE_ON = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "beacon/on");
    public static final ResourceLocation STATE_RED = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "beacon/red");
    public static final ResourceLocation STATE_GREEN = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "beacon/green");
    public static final ResourceLocation STATE_BLUE = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "beacon/blue");
    public static final ResourceLocation STATE_OFF = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "beacon/off");
    public static final String NBT_MODE = "mode";

    private Mode mode = Mode.OFF;

    public enum Mode {
        OFF,
        RED,
        GREEN,
        BLUE
    }


    public Mode getMode() {
        refreshState();
        return mode;
    }

    public void setMode(Mode mode) {
        refreshState();
        this.mode = mode;
        changed();
    }

    public boolean getLightOn() {
        refreshState();
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
        changed();
    }
}
