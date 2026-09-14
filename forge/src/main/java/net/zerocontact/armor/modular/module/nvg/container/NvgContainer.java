package net.zerocontact.armor.modular.module.nvg.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.zerocontact.ZeroContact;

public class NvgContainer implements INBTSerializable<CompoundTag> {
    public static final ResourceLocation STATE_ON = new ResourceLocation(ZeroContact.MOD_ID, "nvg/on");
    public static final ResourceLocation STATE_OFF = new ResourceLocation(ZeroContact.MOD_ID, "nvg/off");
    public static final ResourceLocation STATE_NO_POWER = new ResourceLocation(ZeroContact.MOD_ID, "nvg/no_power");
    public static final String NBT_ENABLED = "enabled";
    public static final String NBT_BATTERY = "battery";
    private static final String NBT_POWER = "power_status";
    private boolean enabled = false;
    private final float maxBattery = 12000;
    private float battery = maxBattery;
    // A synchronized presentation snapshot, never a reference to another module.
    private PowerStatus powerStatus = new PowerStatus(PowerSource.INTERNAL, battery, maxBattery);

    public enum PowerSource { NONE, INTERNAL, EXTERNAL }

    public record PowerStatus(PowerSource source, float remaining, float capacity) {
        public boolean powered() {
            return source != PowerSource.NONE && remaining > 0;
        }
    }

    public PowerStatus getPowerStatus() {
        return powerStatus;
    }

    /** Server only: recompute even while disabled and before validating an action. */
    public void refreshPowerStatus(float externalRemaining, float externalCapacity) {
        powerStatus = externalRemaining > 0
                ? new PowerStatus(PowerSource.EXTERNAL, externalRemaining, externalCapacity)
                : new PowerStatus(battery > 0 ? PowerSource.INTERNAL : PowerSource.NONE, battery, maxBattery);
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getMaxBattery() {
        return powerStatus.capacity();
    }

    public float getBattery() {
        return powerStatus.remaining();
    }

    public boolean outOfPower() {
        return !powerStatus.powered();
    }

    /** Server only. Refresh snapshots after all consumers have drawn power. */
    public void consumeInternalBattery() {
        battery = Math.max(0, battery - 1);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(NBT_ENABLED, enabled);
        tag.putFloat(NBT_BATTERY, battery);
        CompoundTag power = new CompoundTag();
        power.putString("source", powerStatus.source().name());
        power.putFloat("remaining", powerStatus.remaining());
        power.putFloat("capacity", powerStatus.capacity());
        tag.put(NBT_POWER, power);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        this.enabled = arg.getBoolean(NBT_ENABLED);
        this.battery = arg.contains(NBT_BATTERY, Tag.TAG_ANY_NUMERIC)
                ? clampBattery(arg.getFloat(NBT_BATTERY), maxBattery) : maxBattery;
        refreshPowerStatus(0, 0); // Legacy saves have no presentation snapshot.
        if (arg.contains(NBT_POWER, Tag.TAG_COMPOUND)) {
            CompoundTag power = arg.getCompound(NBT_POWER);
            try {
                PowerSource source = PowerSource.valueOf(power.getString("source"));
                float capacity = power.getFloat("capacity");
                if (Float.isFinite(capacity) && capacity > 0) {
                    powerStatus = new PowerStatus(source,
                            clampBattery(power.getFloat("remaining"), capacity), capacity);
                }
            } catch (IllegalArgumentException ignored) {
                // Unknown snapshot format: retain the internal-battery fallback.
            }
        }
        // On the server this snapshot must be recomputed before use; on the client
        // it is the authoritative display state received through ModuleSyncService.
    }

    private static float clampBattery(float value, float capacity) {
        return Float.isFinite(value) ? Math.max(0, Math.min(value, capacity)) : 0;
    }
}
