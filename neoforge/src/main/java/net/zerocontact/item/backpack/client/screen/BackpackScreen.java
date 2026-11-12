package net.zerocontact.item.backpack.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.zerocontact.ZeroContact;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.events.ClientForgeEventBus;
import net.zerocontact.item.backpack.menu.BackpackContainerMenu;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.c2s.ToggleBackpackPacket;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

import java.util.Optional;

@EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
public class BackpackScreen extends AbstractContainerScreen<BackpackContainerMenu> {

    private static final int OUTLINE_COLOR = 0x55141414;
    private static final int SLOT_BG_COLOR = 0x55a1a1a1;
    private static final int VIGNETTE_COLOR = 0x88000000;

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiLayerEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof BackpackScreen) {
            if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
                event.setCanceled(true);
            }
        }
    }

    private int guiWidthXMax;
    private int guiHeightYMax;

    public BackpackScreen(BackpackContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        this.imageWidth = menu.guiWidth;
        this.imageHeight = menu.guiHeight;
        super.init();
        this.titleLabelX = (getXSize() - this.font.width(title)) / 2;
        this.guiWidthXMax = getGuiLeft() + menu.guiWidth;
        this.guiHeightYMax = getGuiTop() + menu.guiHeight;
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, width, height, 0, VIGNETTE_COLOR);
        guiGraphics.fill(getGuiLeft(),getGuiTop(),guiWidthXMax,guiHeightYMax,0x88000000);
        drawBgOutline(guiGraphics, guiWidthXMax, guiHeightYMax);
        drawSlotBg(menu, guiGraphics);
        drawEquipmentLabel(guiGraphics);
        int previewRight = getGuiLeft() - 12;
        int previewLeft = Math.max(0, previewRight - 64);
        if (previewRight - previewLeft < 32) return;
        Optional.ofNullable(Minecraft.getInstance().player).ifPresent(
                player -> InventoryScreen.renderEntityInInventoryFollowsMouse(
                        guiGraphics,
                        previewLeft,
                        getGuiTop() + 8,
                        previewRight,
                        guiHeightYMax - 8,
                        Math.min(64, (imageHeight - 16) / 2),
                        0.0625F,
                        mouseX,
                        mouseY,
                        player
                )
        );
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics,partialTick,mouseX,mouseY);
    }

    private void drawEquipmentLabel(@NotNull GuiGraphics guiGraphics) {
        int previewHalfWidth = BackpackContainerMenu.EQUIPMENT_PREVIEW_SIZE / 2;
        drawEquipmentPreview(guiGraphics, menu.backpackRenderStack, getGuiLeft() + previewHalfWidth, false);
        int rigsX = getGuiLeft() + menu.rigStackStart + previewHalfWidth;
        drawEquipmentPreview(guiGraphics, menu.rigsRenderStack, rigsX, true);
    }

    private void drawEquipmentPreview(GuiGraphics guiGraphics, ItemStack stack, int centerX, boolean rotate) {
        if (stack == null || stack.isEmpty()) return;
        var renderer = GeoRenderProvider.of(stack).getGeoItemRenderer();
        if (renderer == null) return;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, getGuiTop() + 64, 10);
        if (rotate) guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(180));
        float scale = BackpackContainerMenu.EQUIPMENT_PREVIEW_SIZE;
        guiGraphics.pose().scale(scale, -scale, scale);
        // Cancel GeoItemRenderer's half-unit X offset so rotation keeps the preview centered.
        guiGraphics.pose().translate(-0.5, 0, 0);
        renderer.renderByItem(stack, ItemDisplayContext.GUI, guiGraphics.pose(),
                guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY);
        guiGraphics.pose().popPose();
    }

    private void drawSlotBg(BackpackContainerMenu menu, GuiGraphics guiGraphics) {
        for (Slot slot : menu.slots) {
            int slotX = leftPos + slot.x;
            int slotY = topPos + slot.y;
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, SLOT_BG_COLOR);
            guiGraphics.renderOutline(slotX, slotY +16, 16, 1, 0x33ffffff);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void drawBgOutline(@NotNull GuiGraphics guiGraphics, int guiWidthMax, int guiHeightMax) {
        guiGraphics.fill(getGuiLeft()-2, getGuiTop(), guiWidthMax+2, getGuiTop() - 2, OUTLINE_COLOR);
        guiGraphics.fill(getGuiLeft()-2, guiHeightMax, guiWidthMax+2, guiHeightMax + 2, OUTLINE_COLOR);
        guiGraphics.fill(getGuiLeft(), getGuiTop(), getGuiLeft() - 2, guiHeightMax, OUTLINE_COLOR);
        guiGraphics.fill(guiWidthMax, getGuiTop(), guiWidthMax + 2, guiHeightMax, OUTLINE_COLOR);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyBindingHandler.TOGGLE_BACKPACK_KEY.matches(keyCode, scanCode)) {
            ClientForgeEventBus.suppressBackpackOpenUntilKeyRelease();
            closeBackpack();
            return true;
        }

        if (Minecraft.getInstance().options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))
                || keyCode == GLFW.GLFW_KEY_ESCAPE
        ) {
            closeBackpack();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void closeBackpack() {
        Optional.ofNullable(Minecraft.getInstance().player).ifPresent(LocalPlayer::closeContainer);
        ModMessages.sendToServer(new ToggleBackpackPacket(false));
    }
}
