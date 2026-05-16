package net.zerocontact.events;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.PlatePackets;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientPlateBundleEvent {
    @SubscribeEvent
    public static void onExtractPlate(ScreenEvent.KeyPressed event) {
        processArmorSlotItem(event, (inventoryScreen, slot) -> {
            if (event.getKeyCode() == GLFW.GLFW_KEY_SPACE) {
                ModMessages.sendToServer(new PlatePackets.PlateExtractor(slot.getSlotIndex()));
                inventoryScreen.getMenu().broadcastChanges();
            }
        });

    }

    @SubscribeEvent
    public static void onInsertPlate(ScreenEvent.MouseButtonPressed.Pre event) {
        processArmorSlotItem(event, ((inventoryScreen, slot) -> {
            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                ModMessages.sendToServer(new PlatePackets.PlateInserter(slot.getSlotIndex()));
                inventoryScreen.getMenu().broadcastChanges();
                event.setCanceled(true);
            }
        }));
    }

    @SubscribeEvent
    public static void onScrollPlateIndex(ScreenEvent.MouseScrolled.Post mouseScrolled) {
        processArmorSlotItem(mouseScrolled, (inventoryScreen, slot) -> {
            int invert = slot.getItem().getOrCreateTag().getInt("PointingSlot") == 0 ? 1 : 0;
            slot.getItem().getOrCreateTag().putInt("PointingSlot", invert);
            ModMessages.sendToServer(new PlatePackets.IndexUpdater(slot.getSlotIndex(), invert));
        });
    }

    private static <T extends ScreenEvent> void processArmorSlotItem(T event, BiConsumer<InventoryScreen, Slot> stackConsumer) {
        Screen screen = event.getScreen();
        if (!(screen instanceof InventoryScreen)) return;
        Slot slotUnderMouse = ((InventoryScreen) screen).getSlotUnderMouse();
        if (slotUnderMouse != null) {
            ItemStack slotStack = slotUnderMouse.getItem();
            if (slotStack.getItem() instanceof BaseArmorGeoImpl baseArmorGeo && baseArmorGeo.getArmorType().equals(IEquipmentTypeTag.EquipmentType.PLATE_CARRIER)) {
                stackConsumer.accept((InventoryScreen) screen, slotUnderMouse);
            }
        }
    }
}