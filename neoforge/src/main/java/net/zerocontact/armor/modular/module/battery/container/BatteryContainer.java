package net.zerocontact.armor.modular.module.battery.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.armor.modular.container.StackModuleState;

public class BatteryContainer extends StackModuleState {
    public BatteryContainer() {
        super("battery");
    }

    public BatteryContainer(ItemStack stack) {
        this();
        bind(stack);
    }

    private static final String NBT_BATTERY = "battery";

    private final int maxBattery = 12000;
    private int battery = maxBattery;

    public int getMaxBattery() {
        refreshState();
        return maxBattery;
    }

    public int getBattery() {
        refreshState();
        return battery;
    }

    public boolean outOfPower() {
        refreshState();
        return battery <= 0;
    }

    public void tick() {
        refreshState();
        battery = Math.max(0, battery - 1);
        changed();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(NBT_BATTERY, battery);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        this.battery = arg.contains(NBT_BATTERY) ? Math.max(0, Math.min(maxBattery, arg.getInt(NBT_BATTERY))) : maxBattery;
        changed();
    }
}
