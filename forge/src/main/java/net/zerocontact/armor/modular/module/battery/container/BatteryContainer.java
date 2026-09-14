package net.zerocontact.armor.modular.module.battery.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class BatteryContainer implements INBTSerializable<CompoundTag> {
    private static final String NBT_BATTERY = "battery";

    private final int maxBattery = 12000;
    private int battery = maxBattery;

    public int getMaxBattery() {
        return maxBattery;
    }

    public int getBattery() {
        return battery;
    }

    public boolean outOfPower() {
        return battery <= 0;
    }

    public void tick() {
        battery = Math.max(0, battery - 1);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(NBT_BATTERY, battery);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        this.battery = arg.getInt(NBT_BATTERY);
    }
}
