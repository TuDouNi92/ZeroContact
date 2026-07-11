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
import net.zerocontact.capability.CapabilityRegistries;
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
        if (IGun.getIGunOrNull(checkStack)!=null) {
            checkStack.getCapability(CapabilityRegistries.CARTRIDGE).ifPresent(cap->{
                String ammoVariantId = cap.getAmmoVariantInGun(checkStack);
                MutableComponent ammoLabel = Component.translatable("tooltip.zerocontact.gun.ammoVariant").withStyle(ChatFormatting.GOLD).append(":");
                ItemStack ammoStack = cap.getDefaultStack(ammoVariantId);
                Component ammoDesrciption = Component.literal("\uD83E\uDC35 ").append(Component.translatable(ammoStack.getDescriptionId())).withStyle(ChatFormatting.YELLOW);
                if (!(ammoStack.getItem() instanceof GenerateAmmo))
                    ammoDesrciption = Component.translatable("hud.zerocontact.ammo.default").withStyle(ChatFormatting.YELLOW);
                ammoLabel.append(ammoDesrciption);
                event.getToolTip().add(ammoLabel);
            });
        }
    }
}
