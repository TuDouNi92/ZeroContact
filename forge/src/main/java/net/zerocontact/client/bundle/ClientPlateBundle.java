package net.zerocontact.client.bundle;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import org.jetbrains.annotations.NotNull;

public class ClientPlateBundle implements ClientTooltipComponent {
    private final NonNullList<ItemStack> items;
    private final ItemStack armorStack;

    public ClientPlateBundle(PlateBundle bundleTooltip) {
        this.items = bundleTooltip.items();
        this.armorStack = bundleTooltip.armorStack();
    }

    private int gridSizeX() {
        return Math.max(2, (int) Math.ceil(Math.sqrt((double) this.items.size() + (double) 1.0F)));
    }

    private int gridSizeY() {
        return 1;
    }

    @Override
    public int getHeight() {
        return this.gridSizeY() * 20 + 2 + 4;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return this.gridSizeX() * 18 + 2;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
        int width = gridSizeX();
        int k = 0;
        for (int i = 0; i < width; ++i) {
            this.renderSlot(x + i * 18 + 1, y, k++, guiGraphics, font);
        }
        this.drawBorder(x, y, gridSizeX(), gridSizeY(), guiGraphics);
    }

    private void renderSlot(int x, int y, int renderIndex, GuiGraphics guiGraphics, Font font) {
        if (renderIndex >= this.items.size()) {
            this.blit(guiGraphics, x, y, Texture.SLOT);
        } else {
            ItemStack itemStack = this.items.get(renderIndex);
            this.blit(guiGraphics, x, y, Texture.SLOT);
            guiGraphics.renderItem(itemStack, x + 1, y + 1, renderIndex);
            guiGraphics.renderItemDecorations(font, itemStack, x + 1, y + 1);
            if (renderIndex == BaseArmorGeoImpl.getSelectedPlate(armorStack)) {
                AbstractContainerScreen.renderSlotHighlight(guiGraphics, x + 1, y + 1, 0);
            }
        }
    }

    private void blit(GuiGraphics guiGraphics, int x, int y, Texture texture) {
        guiGraphics.blit(ClientBundleTooltip.TEXTURE_LOCATION, x, y, 0, (float) texture.x, (float) texture.y, texture.w, texture.h, 128, 128);
    }

    private void drawBorder(int x, int y, int slotWidth, int slotHeight, GuiGraphics guiGraphics) {
        this.blit(guiGraphics, x, y, Texture.BORDER_CORNER_TOP);
        this.blit(guiGraphics, x + slotWidth * 18 + 1, y, Texture.BORDER_CORNER_TOP);

        for (int i = 0; i < slotWidth; ++i) {
            this.blit(guiGraphics, x + 1 + i * 18, y, Texture.BORDER_HORIZONTAL_TOP);
            this.blit(guiGraphics, x + 1 + i * 18, y + slotHeight * 20, Texture.BORDER_HORIZONTAL_BOTTOM);
        }

        for (int i = 0; i < slotHeight; ++i) {
            this.blit(guiGraphics, x, y + i * 20 + 1, Texture.BORDER_VERTICAL);
            this.blit(guiGraphics, x + slotWidth * 18 + 1, y + i * 20 + 1, Texture.BORDER_VERTICAL);
        }

        this.blit(guiGraphics, x, y + slotHeight * 20, Texture.BORDER_CORNER_BOTTOM);
        this.blit(guiGraphics, x + slotWidth * 18 + 1, y + slotHeight * 20, Texture.BORDER_CORNER_BOTTOM);
    }

    enum Texture {
        SLOT(0, 0, 18, 20),
        BLOCKED_SLOT(0, 40, 18, 20),
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 60, 1, 1);

        public final int x;
        public final int y;
        public final int w;
        public final int h;

        Texture(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
