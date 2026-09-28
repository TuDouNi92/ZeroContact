package net.zerocontact.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.client.gui.WorkbenchScreen;
import net.zerocontact.datagen.model.RecipePOJO;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ScrollList extends AbstractSelectionList<ScrollList.GearEntry> {
    private static final int BASE_ROW_HEIGHT = 36;
    private static final int INGREDIENT_ROW_HEIGHT = 22;
    private static final int INGREDIENTS_PER_ROW = 4;
    private static final int RECIPE_START_X = 32;
    private final WorkbenchScreen screen;
    private GearEntry hoveredEntry;

    public ScrollList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, WorkbenchScreen screen) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.centerListVertically = false;
        this.screen = screen;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }

    public static class GearEntry extends ContainerObjectSelectionList.Entry<GearEntry> {
        public final Item gearItem;
        private final String gearName;
        public final LinkedHashMap<ItemStack, Integer> recipes = new LinkedHashMap<>();
        private final Font font = Minecraft.getInstance().font;
        private final ScrollList parent;
        public final int recipeIndex;
        private final List<IngredientLayout> ingredientLayouts = new ArrayList<>();
        private int recipeRowCount = 1;

        private record IngredientLayout(ItemStack item, int count, int x, int row) {
        }

        public GearEntry(RecipePOJO gearRecipeData, ScrollList parent, int recipeIndex) {
            this.parent = parent;
            this.gearItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(gearRecipeData.gearId));
            this.recipeIndex = recipeIndex;
            if (this.gearItem != null) {
                this.gearName = Component.translatable(this.gearItem.getDescriptionId()).getString();
                for (RecipePOJO.IngredientItems ingredientItems : gearRecipeData.ingredientItems) {
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ingredientItems.itemId));
                    if (item == null) continue;
                    recipes.put(new ItemStack(item), ingredientItems.neededCount);
                }
            } else {
                this.gearName = "";
            }
            layoutIngredients();
        }

        private void layoutIngredients() {
            int availableWidth = parent.getRowWidth() - RECIPE_START_X - 8;
            int x = 0;
            int row = 0;
            int itemsInRow = 0;
            for (var recipe : recipes.entrySet()) {
                int cellWidth = Math.max(28, 24 + font.width("X" + recipe.getValue()));
                if (itemsInRow == INGREDIENTS_PER_ROW || (itemsInRow > 0 && x + cellWidth > availableWidth)) {
                    row++;
                    x = 0;
                    itemsInRow = 0;
                }
                ingredientLayouts.add(new IngredientLayout(recipe.getKey(), recipe.getValue(), x, row));
                x += cellWidth;
                itemsInRow++;
            }
            recipeRowCount = row + 1;
        }

        public int getRowHeight() {
            return BASE_ROW_HEIGHT + (recipeRowCount - 1) * INGREDIENT_ROW_HEIGHT;
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        private void renderGearIcon(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
            guiGraphics.renderFakeItem(stack, x, y);
        }

        private void renderRecipeIcons(GuiGraphics guiGraphics, int x, int y) {
            for (IngredientLayout layout : ingredientLayouts) {
                int itemX = x + layout.x();
                int itemY = y + layout.row() * INGREDIENT_ROW_HEIGHT;
                guiGraphics.renderFakeItem(layout.item(), itemX, itemY);
                guiGraphics.drawString(font, "X" + layout.count(), itemX + 18, itemY + 8, 0x1bd60f);
            }
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderGearIcon(guiGraphics, new ItemStack(gearItem), left + 8, top + 6);
            guiGraphics.drawString(font, gearName, left + 32, top + 4, 0x1bd60f);
            renderRecipeIcons(guiGraphics, left + RECIPE_START_X, top + 12);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            parent.setFocused(this);
            return true;
        }
    }

    @Override
    protected int getMaxPosition() {
        int totalHeight = headerHeight;
        for (GearEntry entry : children()) {
            totalHeight += entry.getRowHeight();
        }
        return totalHeight;
    }

    @Override
    protected int getRowTop(int index) {
        int top = y0 + 4 - (int) getScrollAmount() + headerHeight;
        for (int i = 0; i < index; i++) {
            top += children().get(i).getRowHeight();
        }
        return top;
    }

    @Override
    protected int getRowBottom(int index) {
        return getRowTop(index) + children().get(index).getRowHeight();
    }

    @Override
    protected void renderList(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int rowLeft = getRowLeft();
        int rowWidth = getRowWidth();
        for (int i = 0; i < children().size(); i++) {
            int rowTop = getRowTop(i);
            int rowBottom = getRowBottom(i);
            if (rowBottom >= y0 && rowTop <= y1) {
                renderItem(guiGraphics, mouseX, mouseY, partialTick, i, rowLeft, rowTop,
                        rowWidth, children().get(i).getRowHeight() - 4);
            }
        }
    }

    @Override
    protected void renderItem(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick,
                              int index, int left, int top, int width, int height) {
        GearEntry entry = children().get(index);
        boolean hovering = entry == hoveredEntry;
        entry.renderBack(guiGraphics, index, top, left, width, height, mouseX, mouseY, hovering, partialTick);
        if (isSelectedItem(index)) {
            renderSelection(guiGraphics, top, width, height, isFocused() ? -1 : -8355712, -16777216);
        }
        entry.render(guiGraphics, index, top, left, width, height, mouseX, mouseY, hovering, partialTick);
    }

    private GearEntry entryAtPosition(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY) || mouseX >= getScrollbarPosition()) {
            return null;
        }
        for (int i = 0; i < children().size(); i++) {
            if (mouseY >= getRowTop(i) && mouseY < getRowBottom(i)) {
                return children().get(i);
            }
        }
        return null;
    }

    @Override
    protected void ensureVisible(@NotNull GearEntry entry) {
        int index = children().indexOf(entry);
        if (index < 0) {
            return;
        }
        int top = getRowTop(index);
        int bottom = getRowBottom(index);
        if (top < y0 + 4) {
            setScrollAmount(getScrollAmount() + top - y0 - 4);
        } else if (bottom > y1) {
            setScrollAmount(getScrollAmount() + bottom - y1);
        }
    }

    @Override
    protected GearEntry getHovered() {
        return hoveredEntry;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        hoveredEntry = entryAtPosition(mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (hoveredEntry != null) {
            guiGraphics.renderTooltip(screen.getMinecraft().font, hoveredEntry.gearItem.getDefaultInstance(), mouseX, mouseY);
        }
    }

    //Fking bugjump, a scrollbar blocked me for the entire day
    @Override
    protected int getScrollbarPosition() {
        return this.x1 + 2;
    }

    @Override
    public int getRowLeft() {
        return this.x0;
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    public void addGearEntry(@NotNull GearEntry entry) {
        this.addEntry(entry);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean onScrollbar = button == 0 && getMaxScroll() > 0
                && mouseY >= y0 && mouseY <= y1
                && mouseX >= getScrollbarPosition() && mouseX < getScrollbarPosition() + 6;
        if (!isMouseOver(mouseX, mouseY) && !onScrollbar) {
            return false;
        }
        screen.setFocused(this);
        updateScrollingState(mouseX, mouseY, button);
        GearEntry entry = entryAtPosition(mouseX, mouseY);
        if (entry != null) {
            boolean clicked = entry.mouseClicked(mouseX, mouseY, button);
            if (clicked) {
                setDragging(true);
            }
            return clicked;
        }
        return onScrollbar;
    }
}
