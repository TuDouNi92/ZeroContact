package net.zerocontact.api.armor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.animation_data.AnimateData;
import net.zerocontact.capability.TagUtil;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.s2c.ToggleVisorResultPacket;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface Toggleable {
    ResourceLocation getVisorTexture();

    AnimateData.VisorAnimateData readAnimData(ItemStack stack);

    void saveAnimData(AnimateData.VisorAnimateData animateData, ItemStack stack);

    default void saveStatus(ItemStack stack, boolean switchOn) {
        TagUtil.updateTag(stack,compoundTag -> {
            compoundTag.putBoolean("VisorOn", switchOn);
        });
    }

    default Boolean readStatus(ItemStack stack, String key) {
        return TagUtil.getOrCreateTag(stack).getBoolean(key);
    }

    default void flipState(Toggleable toggleable, ItemStack stack) {
        boolean state;
        state = toggleable.readStatus(stack, "VisorOn");
        toggleable.saveStatus(stack, !state);
        toggleable.triggered(stack,true);
    }

    default void triggered(ItemStack stack, boolean toggleState) {
        TagUtil.updateTag(stack,compoundTag -> compoundTag.putBoolean("Toggle", toggleState));
    }

    default boolean getTriggered(ItemStack stack) {
        return TagUtil.getOrCreateTag(stack).getBoolean("Toggle");
    }

    interface Backpack {
        void setToggling(ItemStack stack, boolean isOpen);

        boolean getToggling(ItemStack stack);
    }

    @EventBusSubscriber(modid = ZeroContact.MOD_ID)
    class VisorListener {
        @SubscribeEvent
        public static void visorEquip(LivingEquipmentChangeEvent equipmentChangeEvent) {
            if (!(equipmentChangeEvent.getEntity() instanceof ServerPlayer player)) return;
            ItemStack newGear = equipmentChangeEvent.getTo();
            EquipmentSlot slot = equipmentChangeEvent.getSlot();
            if (slot == EquipmentSlot.HEAD && (newGear.getItem() instanceof Toggleable helmet)) {
                ModMessages.sendToPlayer(new ToggleVisorResultPacket(helmet.readAnimData(newGear)), player);
            }
        }
    }
}
