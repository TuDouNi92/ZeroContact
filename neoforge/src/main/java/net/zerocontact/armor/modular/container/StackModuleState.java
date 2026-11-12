package net.zerocontact.armor.modular.container;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Binds a mutable capability view to the stack's persistent component data. */
public abstract class StackModuleState {
    private static final String STATE_KEY = "zerocontact:module_state";
    private final String trait;
    private ItemStack owner;
    private CompoundTag lastState;
    private boolean reading;

    protected StackModuleState(String trait) {
        this.trait = trait;
    }

    /** Call after the subclass's defaults have been initialized. */
    protected final void bind(ItemStack stack) {
        owner = stack;
        refreshState();
    }

    protected final void refreshState() {
        if (owner == null || reading) return;
        CompoundTag state = owner.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getCompound(STATE_KEY).getCompound(trait);
        if (state.equals(lastState)) return;
        lastState = state.copy();
        reading = true;
        try {
            deserializeNBT(state);
        } finally {
            reading = false;
        }
    }

    protected final void changed() {
        if (owner == null || reading) return;
        CompoundTag state = serializeNBT();
        if (state.equals(lastState)) return;
        lastState = state.copy();
        CustomData.update(DataComponents.CUSTOM_DATA, owner, tag -> {
            CompoundTag states = tag.getCompound(STATE_KEY);
            states.put(trait, state);
            tag.put(STATE_KEY, states);
        });
    }

    public abstract CompoundTag serializeNBT();

    public abstract void deserializeNBT(CompoundTag tag);
}
