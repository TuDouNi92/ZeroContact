package net.zerocontact.events;

import com.tacz.guns.api.item.IGun;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.CaliberHelper;
import net.zerocontact.caliber.capability.CapabilityRegistry;
import net.zerocontact.client.tooltip.AdvancedAmmoInfoComponents;
import net.zerocontact.config.ModConfigs;
import net.zerocontact.datagen.loader.ZPackManager;
import net.zerocontact.forge_registries.ItemRegistry;
import net.zerocontact.item.ammo.GenerateAmmo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
public class TooltipHandlerEvent {
    public static final String SHOW_BULLET_DATA_LABEL = "tooltip.zerocontact.bullet_data";

    @SubscribeEvent
    public static void onToolTip(ItemTooltipEvent event) {
        appendPackInfo(event);
        appendAmmoInfoToGun(event);
    }

    private static void appendPackInfo(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        ItemRegistry.ITEMS_REG_TAB.entrySet().stream()
                .filter(entry -> entry.getKey().get().equals(item))
                .findFirst()
                .ifPresent(entry -> {
                    MutableComponent packHeader = Component.translatable("tooltip.zerocontact.pack." + entry.getValue())
                            .withStyle(ChatFormatting.BLUE)
                            .withStyle(ChatFormatting.ITALIC);
                    MutableComponent packHint = Component.translatable("tooltip.zerocontact.pack_hint").withStyle(ChatFormatting.GRAY);
                    List<MutableComponent> packInfo = new ArrayList<>(List.of(packHeader, packHint));
                    if (Screen.hasShiftDown()) {
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
        if (!ModConfigs.CLIENT.ammoTypeTooltip().get()) return;
        ItemStack checkStack = event.getItemStack();
        if (IGun.getIGunOrNull(checkStack) != null) {
            Optional.ofNullable(checkStack.getCapability(CapabilityRegistry.CARTRIDGE)).ifPresent(cap -> {
                String ammoVariantId = cap.getAmmoVariantInGun(checkStack);
                CaliberHelper.Caliber caliber = AmmoInjector.get().readCxtFromStack(checkStack).caliber();
                MutableComponent ammoLabel = Component.translatable("tooltip.zerocontact.gun.ammoVariant").withStyle(ChatFormatting.GOLD).append(":");
                ItemStack ammoStack = cap.getDefaultStack(ammoVariantId);
                Component ammoName = Component.literal("\uD83E\uDC35 ").append(Component.translatable(ammoStack.getDescriptionId())).withStyle(ChatFormatting.YELLOW);
                Component ammoAdvancedInfo = Component.translatable(SHOW_BULLET_DATA_LABEL).withStyle(ChatFormatting.GRAY);

                if (!(ammoStack.getItem() instanceof GenerateAmmo)) {
                    ammoName = Component.translatable("hud.zerocontact.ammo.default").withStyle(ChatFormatting.YELLOW);
                    ammoLabel.append(ammoName);
                    event.getToolTip().add(ammoLabel);
                } else {
                    ammoLabel.append(ammoName);
                    event.getToolTip().addAll(List.of(ammoName, ammoAdvancedInfo));
                    if (Screen.hasShiftDown()) {
                        List<Component> fullAmmoData = AdvancedAmmoInfoComponents.create(caliber, false);
                        event.getToolTip().addAll(fullAmmoData);
                    }
                }
            });
        }
    }
}
