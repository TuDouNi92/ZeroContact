package net.zerocontact.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.ClientData;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.client.menu.BackpackContainerMenu;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class BackpackScreen extends AbstractContainerScreen<BackpackContainerMenu> {
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof BackpackScreen) {
            if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
                event.setCanceled(true);
            }
        }
    }
    private int guiWidthMax;
    private int guiHeightMax;
    public BackpackScreen(BackpackContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (getXSize() - this.font.width(title)) / 2;
        this.guiWidthMax = getGuiLeft()+menu.guiWidth;
        this.guiHeightMax = getGuiTop()+menu.guiHeight;
        this.leftPos = (this.width - this.guiWidthMax)/2;
        this.topPos =(this.height - this.guiHeightMax)/2;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xccc3bdbd, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, width, height, 0, 0x88000000);
        guiGraphics.fill(getGuiLeft(), getGuiTop(), guiWidthMax, guiHeightMax, 0x88000000);
        drawBgOutline(guiGraphics, guiWidthMax, guiHeightMax);
        drawSlotBg(menu, guiGraphics);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(getGuiLeft(), getGuiTop() + 64, 10);
        guiGraphics.pose().scale(40.0f, -40.0f, 40.0f);
        IClientItemExtensions extensions = IClientItemExtensions.of(menu.backpackRenderStack);
        extensions.getCustomRenderer().renderByItem(
                menu.backpackRenderStack,
                ItemDisplayContext.GUI,
                guiGraphics.pose(),
                guiGraphics.bufferSource(),
                15728880,
                OverlayTexture.NO_OVERLAY
        );
        guiGraphics.pose().popPose();
        Optional.ofNullable(Minecraft.getInstance().player).ifPresent(
                player -> InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, getGuiLeft() + 20, getGuiTop() + 96, 32, 0, 0, player));
    }


    private void drawSlotBg(BackpackContainerMenu menu, GuiGraphics guiGraphics) {
        for (Slot slot : menu.slots) {
            int slotX = leftPos + slot.x;
            int slotY = topPos + slot.y;
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 2, 0x55FFFFFF);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void drawBgOutline(@NotNull GuiGraphics guiGraphics, int guiWidthMax, int guiHeightMax) {
        guiGraphics.fill(getGuiLeft(), getGuiTop(), guiWidthMax, getGuiTop() - 1, 0xbb888888);
        guiGraphics.fill(getGuiLeft(), guiHeightMax, guiWidthMax, guiHeightMax + 1, 0xbb888888);
        guiGraphics.fill(getGuiLeft(), getGuiTop(), getGuiLeft() - 1, guiHeightMax, 0xbb888888);
        guiGraphics.fill(guiWidthMax, getGuiTop(), guiWidthMax + 1, guiHeightMax, 0xbb888888);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyBindingHandler.TOGGLE_BACKPACK_KEY.matches(keyCode, scanCode)
                || Minecraft.getInstance().options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))
                || keyCode == GLFW.GLFW_KEY_ESCAPE
        ) {
            Optional.ofNullable(Minecraft.getInstance().player).ifPresent(LocalPlayer::closeContainer);
            ModMessages.sendToServer(new NetworkHandler.ToggleBackpackPacket(false));
        }
        ClientData.justCloseBackpack = true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
