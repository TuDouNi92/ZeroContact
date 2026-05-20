package net.zerocontact.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.forge_registries.ItemsRegForge;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {
    @SubscribeEvent
    public static void onToolTip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        ItemsRegForge.ITEMS_REG_TAB.entrySet().stream()
                .filter(entry -> entry.getKey().get().equals(item))
                .findFirst()
                .ifPresent(entry -> event.getToolTip().add(
                        Component.translatable("tooltip.zerocontact.pack." + entry.getValue())
                                .withStyle(ChatFormatting.BLUE)
                                .withStyle(ChatFormatting.ITALIC)
                ));

    }
}
