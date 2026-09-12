package net.zerocontact.armor.modular.module.nvg.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.zerocontact.ZeroContact;

public class NvgContainer implements INBTSerializable<CompoundTag> {
    public static final ResourceLocation STATE_ON = new ResourceLocation(ZeroContact.MOD_ID, "nvg/on");
    public static final ResourceLocation STATE_OFF = new ResourceLocation(ZeroContact.MOD_ID, "nvg/off");
    public static final ResourceLocation STATE_NO_POWER = new ResourceLocation(ZeroContact.MOD_ID, "nvg/no_power");
    public static final String NBT_ENABLED = "enabled";
    public static final String NBT_BATTERY = "battery";
    private boolean enabled = false;
    private float defaultBattery = 12000;
    private float battery = defaultBattery;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getDefaultBattery() {
        return defaultBattery;
    }

    public float getBattery() {
        return battery;
    }

    public void setDefaultBattery(float defaultBattery) {
        this.defaultBattery = defaultBattery;
    }

    public void setBattery(float battery) {
        this.battery = battery;
    }

    public boolean outOfPower() {
        return battery <= 0;
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(NBT_ENABLED, enabled);
        tag.putFloat(NBT_BATTERY, battery);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        this.enabled = arg.getBoolean(NBT_ENABLED);
        this.battery = arg.getFloat(NBT_BATTERY);
    }

    public void tick() {
        battery = Math.max(0, battery - 1);
    }
}
