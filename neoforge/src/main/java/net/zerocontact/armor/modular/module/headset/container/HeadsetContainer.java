package net.zerocontact.armor.modular.module.headset.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.container.StackModuleState;

public class HeadsetContainer extends StackModuleState {
    public HeadsetContainer() {
        super("headset");
    }

    public HeadsetContainer(ItemStack stack) {
        this();
        bind(stack);
    }

    public static final ResourceLocation STATE_ON = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "headset/on");
    public static final ResourceLocation STATE_OFF = ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "headset/off");
    private static final String NBT_ON = "headset_on";
    private boolean headsetOn = false;

    public boolean isHeadsetOn() {
        refreshState();
        return headsetOn;
    }

    public void setHeadsetOn(boolean headsetOn) {
        refreshState();
        this.headsetOn = headsetOn;
        changed();
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
        changed();
    }
}
