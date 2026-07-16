package net.zerocontact.capability;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.api.IRepairKit;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.NotNull;

public class RepairKitCap implements IRepairKit {
    private final CompoundTag tag;
    static final String KIT_REPAIR_TIMER = "kit_repair_timer";
    static final String INTERRUPT = "kit_interrupt";
    static final String TARGET_SLOT = "kit_target_slot";
    static final String KIT_STARTED = "kit_started";

    public RepairKitCap() {
        this.tag = new CompoundTag();
    }


    public int getKitRepairTime() {
        return tag.getInt(KIT_REPAIR_TIMER);
    }

    public void setKitRepairTime(int ticks) {
        tag.putInt(KIT_REPAIR_TIMER, ticks);
    }

    public boolean shouldRepairAtTick() {
        int ticks = getKitRepairTime();
        if (ticks > 0) {
            int reduced = ticks - 1;
            setKitRepairTime(reduced);
        } else {
            stopRepair();
            return false;
        }
        return ticks % 20 == 0;
    }


    public boolean interrupted() {
        return tag.getBoolean(INTERRUPT);
    }

    public void setInterrupt(boolean interrupt) {
        tag.putBoolean(INTERRUPT, interrupt);
    }

    public boolean started() {
        return tag.getBoolean(KIT_STARTED);
    }

    public boolean finished() {
        return started() && getKitRepairTime() <= 0;
    }

    public void setStarted(boolean started) {
        tag.putBoolean(KIT_STARTED, started);
    }

    public int getTargetSlot() {
        return tag.getInt(TARGET_SLOT);
    }

    public void setTargetSlot(int slot) {
        tag.putInt(TARGET_SLOT, slot);
    }

    public void setTargetSlotFromInv(ServerPlayer player, ItemStack kitStack) {
        NonNullList<ItemStack> inv = player.getInventory().items;
        ItemStack armorStack = inv.stream()
                .filter(item -> item.getItem() instanceof ICombatArmorItem)
                .filter(item -> canRepair(kitStack, item))
                .findFirst()
                .orElse(ItemStack.EMPTY);
        if (armorStack.isEmpty()) return;
        setRepairItem(player, armorStack);
    }

    public void setRepairItem(ServerPlayer player, ItemStack stack) {
        int slot = player.getInventory().items.indexOf(stack);
        int neededAmount = stack.getDamageValue();
        int neededTicks = neededAmount * 20;
        setTargetSlot(slot);
        setKitRepairTime(neededTicks);
    }

    public boolean canRepair(@NotNull ItemStack kit, ItemStack slotItemStack) {
        int slotDurability = slotItemStack.getMaxDamage() - slotItemStack.getDamageValue();
        int kitDurability = kit.getMaxDamage() - kit.getDamageValue();
        return kitDurability >= slotDurability;
    }


    public void repair(ServerPlayer player, ItemStack kitStack, ItemStack slotItemStack) {
        kitStack.hurtAndBreak(1, player, playerHolder -> playerHolder.playNotifySound(ModSoundEventsReg.ARMOR_BROKEN_PLATE, SoundSource.PLAYERS, 1.0f, 1.0f));
        slotItemStack.setDamageValue(slotItemStack.getDamageValue() - 1);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS);
    }

    public void stopRepair() {
        setInterrupt(false);
        setStarted(false);
        setKitRepairTime(0);
        setTargetSlot(-1);
    }
}
