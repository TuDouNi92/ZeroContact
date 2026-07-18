package net.zerocontact.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.animation_data.AnimateData;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;

public interface Toggleable {
    ResourceLocation getVisorTexture();

    AnimateData.VisorAnimateData readAnimData(ItemStack stack);

    void saveAnimData(AnimateData.VisorAnimateData animateData, ItemStack stack);

    default void saveStatus(ItemStack stack, boolean switchOn) {
        stack.getOrCreateTag().putBoolean("VisorOn", switchOn);
    }

    default Boolean readStatus(ItemStack stack, String key) {
        return stack.getOrCreateTag().getBoolean(key);
    }

    default void flipState(Toggleable toggleable, ItemStack stack) {
        boolean state;
        state = toggleable.readStatus(stack, "VisorOn");
        toggleable.saveStatus(stack, !state);
        toggleable.triggered(stack,true);
    }

    default void triggered(ItemStack stack, boolean toggleState) {
        stack.getOrCreateTag().putBoolean("Toggle", toggleState);
    }

    default boolean getTriggered(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("Toggle");
    }

    interface Backpack {
        void setToggling(ItemStack stack, boolean isOpen);

        boolean getToggling(ItemStack stack);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    class VisorListener {
        @SubscribeEvent
        public static void visorEquip(LivingEquipmentChangeEvent equipmentChangeEvent) {
            if (!(equipmentChangeEvent.getEntity() instanceof ServerPlayer player)) return;
            ItemStack newGear = equipmentChangeEvent.getTo();
            EquipmentSlot slot = equipmentChangeEvent.getSlot();
            if (slot == EquipmentSlot.HEAD && (newGear.getItem() instanceof Toggleable helmet)) {
                ModMessages.sendToPlayer(new NetworkHandler.ToggleVisorResultPacket(helmet.readAnimData(newGear)), player);
            }
        }
    }
}
