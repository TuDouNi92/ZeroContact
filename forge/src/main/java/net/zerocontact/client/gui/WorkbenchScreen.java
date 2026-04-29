package net.zerocontact.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.zerocontact.client.gui.components.ScrollList;
import net.zerocontact.client.menu.WorkbenchMenu;
import net.zerocontact.datagen.GearRecipeData;
import net.zerocontact.item.block.WorkBenchEntity;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;
import org.jetbrains.annotations.NotNull;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
    private int guiWidthMax;
    private int guiHeightMax;
    private Button button;
    private ScrollList scrollList;
    private int recipeIndex = 0;

    public WorkbenchScreen(WorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.guiWidthMax = this.getGuiLeft() + imageWidth;
        this.guiHeightMax = this.getGuiTop() + imageHeight;
        this.button = PlainTextButton.builder(
                        Component.translatable("gui.zerocontact.workbench.submit"), pressed -> onPressedSubmit(button)
                )
                .bounds(guiWidthMax - 42, guiHeightMax - 14, 36, 12)
                .build();

        button.setAlpha(0);
        button.active = false;
        button.setFGColor(0x1bd60f);
        this.scrollList = new ScrollList(Minecraft.getInstance(), 152, 166, getGuiTop() + 22, getGuiTop() + 150, 36, this);
        for (GearRecipeData data : WorkBenchEntity.recipeData) {
            scrollList.addGearEntry(new ScrollList.GearEntry(data, scrollList, recipeIndex));
            recipeIndex++;
        }
        scrollList.setLeftPos(getGuiLeft() + 12);
        addRenderableWidget(button);
        addRenderableWidget(scrollList);
    }

    private void onPressedSubmit(Button button) {
        ScrollList.GearEntry entry = scrollList.getSelected();
        if (menu.blockEntity != null) {
            if (entry != null) {
                ModMessages.sendToServer(new NetworkHandler.BuyGearsPacket(menu.blockEntity.getBlockPos(), entry.recipeIndex));
            }
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (scrollList.getSelected() != null) {
            button.active = true;
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component title = Component.translatable("gui.zerocontact.workbench.title");
        FormattedCharSequence sequence = title.getVisualOrderText();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.7f, 0.7f, 0.7f);
        guiGraphics.drawString(font, sequence, 12, 12, 0x1bd60f);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, width, height, -1, 0x88000000);
        guiGraphics.fill(getGuiLeft(), getGuiTop(), guiWidthMax, guiHeightMax, 0x88000000);
        guiGraphics.fill(getGuiLeft() + 12, getGuiTop() + 22, guiWidthMax - 12, guiHeightMax - 16, 0x88000000);
        drawBgOutline(guiGraphics, guiWidthMax, guiHeightMax);
    }

    private void drawBgOutline(@NotNull GuiGraphics guiGraphics, int guiWidthMax, int guiHeightMax) {
        guiGraphics.fill(getGuiLeft(), getGuiTop(), guiWidthMax, getGuiTop() - 1, 0xbb888888);
        guiGraphics.fill(getGuiLeft(), guiHeightMax, guiWidthMax, guiHeightMax + 1, 0xbb888888);
        guiGraphics.fill(getGuiLeft(), getGuiTop(), getGuiLeft() - 1, guiHeightMax, 0xbb888888);
        guiGraphics.fill(guiWidthMax, getGuiTop(), guiWidthMax + 1, guiHeightMax, 0xbb888888);
    }
}
