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
import net.zerocontact.datagen.GearRecipeData;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;

public class ScrollList extends AbstractSelectionList<ScrollList.GearEntry> {
    private final WorkbenchScreen screen;
    public ScrollList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, WorkbenchScreen screen) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.centerListVertically=false;
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
        public GearEntry(GearRecipeData gearRecipeData, ScrollList parent, int recipeIndex) {
            this.parent = parent;
            this.gearItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(gearRecipeData.gearId));
            this.recipeIndex = recipeIndex;
            if (this.gearItem != null) {
                this.gearName = Component.translatable(this.gearItem.getDescriptionId()).getString();
                for (GearRecipeData.IngredientItems ingredientItems : gearRecipeData.ingredientItems) {
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ingredientItems.itemId));
                    if (item == null) continue;
                    recipes.put(new ItemStack(item), ingredientItems.neededCount);
                }
            } else {
                this.gearName = "";
            }
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        private void renderGearIcon(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
            guiGraphics.renderFakeItem(stack, x, y);
        }

        private void renderRecipeIcons(GuiGraphics guiGraphics, LinkedHashMap<ItemStack, Integer> recipe, int x, int y) {
            final int[] offsetX = {0};
            recipe.forEach((item, number) -> {
                guiGraphics.pose().pushPose();
//                guiGraphics.pose().scale(0.5f,0.5f,0.5f);
                guiGraphics.renderFakeItem(item, x + offsetX[0], y);
                offsetX[0] += 28;
                guiGraphics.drawString(font, "X" + number, x + offsetX[0] - 12, y + 8, 0x1bd60f);
                guiGraphics.pose().popPose();
            });
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderGearIcon(guiGraphics, new ItemStack(gearItem), left+8, top+6);
            guiGraphics.drawString(font, gearName, left + 32, top + 4, 0x1bd60f);
            renderRecipeIcons(guiGraphics, recipes, left + 32, top + 12);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            GearEntry entry = parent.getEntryAtPosition(mouseX, mouseY);
            if (entry != null) {
                parent.setFocused(entry);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    //Fking bugjump, a scrollbar blocked me for the entire day
    @Override
    protected int getScrollbarPosition() {
        return this.x1+2;
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

}
