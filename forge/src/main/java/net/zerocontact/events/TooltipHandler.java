package net.zerocontact.events;

import com.tacz.guns.api.item.IGun;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.datagen.loader.ZPackManager;
import net.zerocontact.forge_registries.ItemsRegForge;
import net.zerocontact.item.ammo.GenerateAmmo;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipHandler {
    @SubscribeEvent
    public static void onToolTip(ItemTooltipEvent event) {
        appendPackInfo(event);
        appendAmmoInfoToGun(event);
    }

    private static void appendPackInfo(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        ItemsRegForge.ITEMS_REG_TAB.entrySet().stream()
                .filter(entry -> entry.getKey().get().equals(item))
                .findFirst()
                .ifPresent(entry -> {
                    MutableComponent packHeader = Component.translatable("tooltip.zerocontact.pack." + entry.getValue())
                            .withStyle(ChatFormatting.BLUE)
                            .withStyle(ChatFormatting.ITALIC);
                    MutableComponent packHint = Component.translatable("tooltip.zerocontact.pack_hint").withStyle(ChatFormatting.GRAY);
                    List<MutableComponent> packInfo = new ArrayList<>(List.of(packHeader, packHint));
                    if (DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> Screen::hasShiftDown)) {
                        packInfo.addAll(ZPackManager.getInstance().outerPacks
                                .stream()
                                .filter(pack -> pack.tab().equals(entry.getValue()))
                                .findFirst()
                                .map(pack -> List.of(
                                        Component.literal("Author: ").append(pack.author()).withStyle(ChatFormatting.DARK_GRAY),
                                        Component.literal("Version: ").append(pack.version()).withStyle(ChatFormatting.DARK_GRAY)
                                ))
                                .orElse(List.of()));
                    }
                    event.getToolTip().addAll(packInfo);
                });
    }

    private static void appendAmmoInfoToGun(ItemTooltipEvent event) {
        ItemStack checkStack = event.getItemStack();
        if (IGun.getIGunOrNull(checkStack) != null) {
            checkStack.getCapability(CapabilityRegistries.CARTRIDGE).ifPresent(cap -> {
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
