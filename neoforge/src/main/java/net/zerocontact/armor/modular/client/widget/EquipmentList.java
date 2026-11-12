package net.zerocontact.armor.modular.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EquipmentList extends ContainerObjectSelectionList<EquipmentList.EquipmentEntry> {
    private static final int CARD_WIDTH = 75;
    private static final int CARD_HEIGHT = 60;
    private static final int GAP = 4;

    public EquipmentList(Minecraft minecraft, int width, int height, int y0) {
        this(minecraft, width, height, y0, CARD_HEIGHT + GAP);
    }

    public EquipmentList(Minecraft minecraft, int width, int height, int y0, int itemHeight) {
        super(minecraft, width, height, y0, Math.max(itemHeight, CARD_HEIGHT + GAP));
        centerListVertically = false;
    }

    @Override
    protected void renderListBackground(@NotNull GuiGraphics guiGraphics) {
        // EquipmentScreen draws the panel background.
    }

    @Override
    protected void renderListSeparators(@NotNull GuiGraphics guiGraphics) {
        // Keep the list inside the custom panel without vanilla separators.
    }

    public void setCards(List<EquipmentCard> cards) {
        List<EquipmentCard> snapshot = List.copyOf(cards);
        setFocused(null);
        clearEntries();
        setScrollAmount(0);
        int columns = Math.max(1, (getRowWidth() + GAP) / (CARD_WIDTH + GAP));
        for (int start = 0; start < snapshot.size(); start += columns) {
            addEntry(new EquipmentEntry(snapshot.subList(start, Math.min(start + columns, snapshot.size()))));
        }
    }

    @Override
    public int getRowWidth() {
        return Math.max(1, width - 16);
    }

    @Override
    public int getRowLeft() {
        return getX() + GAP;
    }

    @Override
    protected int getScrollbarPosition() {
        return getRight() - 6;
    }

    public static class EquipmentEntry extends Entry<EquipmentEntry> {
        private final List<EquipmentCard> cards;

        public EquipmentEntry(List<EquipmentCard> cards) {
            this.cards = List.copyOf(cards);
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return cards;
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int cardWidth = Math.clamp((width - (long) (cards.size() - 1) * GAP) / Math.max(1, cards.size()), 1, CARD_WIDTH);
            for (int column = 0; column < cards.size(); column++) {
                EquipmentCard card = cards.get(column);
                card.setX(left + column * (cardWidth + GAP));
                card.setY(top);
                card.setWidth(cardWidth);
                card.setHeight(CARD_HEIGHT);
                card.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return cards;
        }
    }
}
