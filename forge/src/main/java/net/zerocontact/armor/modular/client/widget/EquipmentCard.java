package net.zerocontact.armor.modular.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class EquipmentCard extends AbstractButton {
    private final ItemStack renderStack;
    private final Consumer<EquipmentCard> onPress;
    private boolean selected;
    private static final int CARD_BG = 0xaaffffff;
    private static final int CARD_HOVER_BG = 0xccffffff;

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public EquipmentCard(ItemStack renderStack, int x, int y, int width, int height, Component message,
                         Consumer<EquipmentCard> onPress) {
        super(x, y, width, height, message);
        this.renderStack = renderStack.copy();
        this.onPress = Objects.requireNonNull(onPress);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean highlighted = active && (selected || isHoveredOrFocused());

        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height,
                highlighted ? CARD_HOVER_BG : CARD_BG);
        if (highlighted) {
            guiGraphics.renderOutline(getX(), getY(), width, height, 0xffffffff);
        }
        float scale = 2.0f;
        float centerX = getX() + width / 2.0f;
        float centerY = getY() + height / 2.0f - 8;

        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 0);
        pose.scale(scale, scale, 1.0f);
        guiGraphics.renderItem(
                renderStack,
                -8,
                -8
        );
        pose.popPose();
        int padding = 4;

        int i = this.getX() + padding;
        int j = this.getX() + this.getWidth() - padding;

        renderScrollingString(
                guiGraphics,
                Minecraft.getInstance().font,
                this.getMessage(),
                i,
                this.getY() + 36,
                j,
                this.getY() + this.getHeight(),
                active ? 0xffffff : 0xa0a0a0
        );
    }

    @Override
    public void onPress() {
        onPress.accept(this);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
