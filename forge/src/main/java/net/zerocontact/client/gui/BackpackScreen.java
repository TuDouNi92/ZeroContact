package net.zerocontact.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.client.menu.BackpackContainerMenu;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

import static net.zerocontact.ZeroContact.MOD_ID;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class BackpackScreen extends AbstractContainerScreen<BackpackContainerMenu> {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(MOD_ID,"textures/gui/kevlar_bg.png");
    private static final int OUTLINE_COLOR = 0xcc141414;
    private static final int SLOT_BG_COLOR = 0x55a1a1a1;
    private static final int VIGNETTE_COLOR = 0x88000000;

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof BackpackScreen) {
            if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
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
        super.init();
        this.titleLabelX = (getXSize() - this.font.width(title)) / 2;
        this.leftPos = (this.width - menu.guiWidth) / 2;
        this.topPos = (this.height - menu.guiHeight) / 2;
        this.guiWidthXMax = getGuiLeft() + menu.guiWidth;
        this.guiHeightYMax = getGuiTop() + menu.guiHeight;
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, width, height, 0, VIGNETTE_COLOR);
        guiGraphics.blitRepeating(BACKGROUND,getGuiLeft(),getGuiTop(),menu.guiWidth,menu.guiHeight,0,0,16,16,16,16);
        drawBgOutline(guiGraphics, guiWidthXMax, guiHeightYMax);
        drawSlotBg(menu, guiGraphics);
        drawEquipmentLabel(guiGraphics);
        Optional.ofNullable(Minecraft.getInstance().player).ifPresent(
                player -> InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, getGuiLeft() + 12, getGuiTop() + 88, 20, getGuiLeft() + 20 - mouseX, getGuiTop() + 58 - mouseY, player));
    }

    private void drawEquipmentLabel(@NotNull GuiGraphics guiGraphics) {
        if (menu.backpackRenderStack != null && menu.rigsRenderStack != null) {

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(getGuiLeft(), getGuiTop() + 64, 10);
            guiGraphics.pose().scale(24.0f, -24.0f, 24.0f);
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


            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(getGuiLeft()+24 + menu.rigStackStart, getGuiTop() + 64, 10);
            guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(180));
            guiGraphics.pose().scale(24.0f, -24.0f, 24.0f);
            IClientItemExtensions extensions1 = IClientItemExtensions.of(menu.rigsRenderStack);
            extensions1.getCustomRenderer().renderByItem(
                    menu.rigsRenderStack,
                    ItemDisplayContext.GUI,
                    guiGraphics.pose(),
                    guiGraphics.bufferSource(),
                    15728880,
                    OverlayTexture.NO_OVERLAY
            );
            guiGraphics.pose().popPose();

        } else if (menu.backpackRenderStack != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(getGuiLeft(), getGuiTop() + 64, 10);
            guiGraphics.pose().scale(24.0f, -24.0f, 24.0f);
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
        } else if (menu.rigsRenderStack != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(getGuiLeft()+24, getGuiTop() + 64, 10);
            guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(180));
            guiGraphics.pose().scale(24.0f, -24.0f, 24.0f);
            IClientItemExtensions extensions = IClientItemExtensions.of(menu.rigsRenderStack);
            extensions.getCustomRenderer().renderByItem(
                    menu.rigsRenderStack,
                    ItemDisplayContext.GUI,
                    guiGraphics.pose(),
                    guiGraphics.bufferSource(),
                    15728880,
                    OverlayTexture.NO_OVERLAY
            );
            guiGraphics.pose().popPose();
        }
    }


    private void drawSlotBg(BackpackContainerMenu menu, GuiGraphics guiGraphics) {
        for (Slot slot : menu.slots) {
            int slotX = leftPos + slot.x;
            int slotY = topPos + slot.y;
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 2, SLOT_BG_COLOR);
            guiGraphics.renderOutline(slotX,slotY,16,17,0xccffffff);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void drawBgOutline(@NotNull GuiGraphics guiGraphics, int guiWidthMax, int guiHeightMax) {
        guiGraphics.fill(getGuiLeft(), getGuiTop(), guiWidthMax, getGuiTop() - 2, OUTLINE_COLOR);
        guiGraphics.fill(getGuiLeft(), guiHeightMax, guiWidthMax, guiHeightMax + 2, OUTLINE_COLOR);
        guiGraphics.fill(getGuiLeft(), getGuiTop(), getGuiLeft() - 2, guiHeightMax, OUTLINE_COLOR);
        guiGraphics.fill(guiWidthMax, getGuiTop(), guiWidthMax + 2, guiHeightMax, OUTLINE_COLOR);
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
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
