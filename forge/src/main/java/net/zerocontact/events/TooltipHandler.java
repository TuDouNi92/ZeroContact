package net.zerocontact.events;

import com.tacz.guns.api.item.IGun;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.forge_registries.ItemsRegForge;
import net.zerocontact.item.ammo.GenerateAmmo;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {
    @SubscribeEvent
    public static void onToolTip(ItemTooltipEvent event) {
        appendPackName(event);
        appendAmmoInfoToGun(event);
    }

    private static void appendPackName(ItemTooltipEvent event) {
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

    private static void appendAmmoInfoToGun(ItemTooltipEvent event) {
        ItemStack checkStack = event.getItemStack();
        if (checkStack.getItem() instanceof IGun) {
            String ammoVariantId = AmmoInjector.getAmmoVariantInGun(checkStack);
            MutableComponent ammoLabel = Component.translatable("tooltip.zerocontact.gun.ammoVariant").withStyle(ChatFormatting.GOLD).append(":");
            Component ammoValue = Component.translatable(AmmoInjector.getDefaultStack(ammoVariantId).getDescriptionId()).withStyle(ChatFormatting.YELLOW);
            if (!(checkStack.getItem() instanceof GenerateAmmo))
                ammoValue = Component.translatable("hud.zerocontact.ammo.default").withStyle(ChatFormatting.YELLOW);
            ammoLabel.append(ammoValue);
            event.getToolTip().add(ammoLabel);
        }
    }
}
