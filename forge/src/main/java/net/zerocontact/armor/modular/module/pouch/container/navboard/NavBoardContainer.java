package net.zerocontact.armor.modular.module.pouch.container.navboard;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.zerocontact.ZeroContact;

import java.util.Map;

public class NavBoardContainer implements INBTSerializable<CompoundTag> {
    public static final ResourceLocation STATE_ON = new ResourceLocation(ZeroContact.MOD_ID, "navboard/on");
    public static final ResourceLocation STATE_OFF = new ResourceLocation(ZeroContact.MOD_ID, "navboard/off");

    private static final String NBT_STATE = "state";

    private static final int UPDATE_INTERVAL = 20;
    private int ticker = 0;
    private ResourceLocation activeDimension;

    private final Map<Long, NavBoardChunkTile> activeTiles = new Long2ObjectOpenHashMap<>();

    private boolean isEnabled;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        if (isEnabled != enabled) ticker = 0;
        isEnabled = enabled;
        if (!enabled) clearTiles();
    }

    public Map<Long, NavBoardChunkTile> getActiveTiles() {
        return activeTiles;
    }


    /** Server-side scheduling only; true on the first active tick and every 20 ticks. */
    public boolean tick() {
        if (!isEnabled) {
            ticker = 0;
            return false;
        }
        if (ticker > 0) {
            ticker--;
            return false;
        }
        ticker = UPDATE_INTERVAL - 1;
        return true;
    }

    public void clearTiles() {
        activeTiles.clear();
        activeDimension = null;
    }

    public void prepareTiles(ResourceLocation dimension) {
        if (!dimension.equals(activeDimension)) {
            clearTiles();
            activeDimension = dimension;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(NBT_STATE, isEnabled);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag arg) {
        setEnabled(arg.getBoolean(NBT_STATE));
        ticker = 0;
        clearTiles();

    }
}
