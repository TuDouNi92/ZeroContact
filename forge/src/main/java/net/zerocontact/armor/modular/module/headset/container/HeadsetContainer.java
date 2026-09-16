package net.zerocontact.armor.modular.module.headset.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.zerocontact.ZeroContact;

public class HeadsetContainer implements INBTSerializable<CompoundTag> {
    public static final ResourceLocation STATE_ON = new ResourceLocation(ZeroContact.MOD_ID, "headset/on");
    public static final ResourceLocation STATE_OFF = new ResourceLocation(ZeroContact.MOD_ID, "headset/off");
    private static final String NBT_ON = "headset_on";
    private boolean headsetOn = false;

    public boolean isHeadsetOn() {
        return headsetOn;
    }

    public void setHeadsetOn(boolean headsetOn) {
        this.headsetOn = headsetOn;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(NBT_ON, headsetOn);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        headsetOn = arg.getBoolean(NBT_ON);
    }
}
