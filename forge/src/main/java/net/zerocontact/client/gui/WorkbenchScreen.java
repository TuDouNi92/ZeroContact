package net.zerocontact.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.client.gui.components.ScrollList;
import net.zerocontact.client.menu.WorkbenchMenu;
import net.zerocontact.datagen.GearRecipeData;
import net.zerocontact.item.block.WorkBenchEntity;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
    private int guiWidthMax;
    private int guiHeightMax;
    private Button submitButton;
    private ScrollList scrollList;
    private final Set<GearRecipeData> currentData = new HashSet<>();

    public WorkbenchScreen(WorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public enum Tab {
        ARMOR(IEquipmentTypeTag.EquipmentType.ARMOR, IEquipmentTypeTag.EquipmentType.PLATE_CARRIER, IEquipmentTypeTag.EquipmentType.HELMET),
        PLATE(IEquipmentTypeTag.EquipmentType.PLATE),
        LOADOUTS(IEquipmentTypeTag.EquipmentType.BACKPACK, IEquipmentTypeTag.EquipmentType.RIGS),
        AMMO(IEquipmentTypeTag.EquipmentType.AMMO),
        ACCESSORIES(IEquipmentTypeTag.EquipmentType.ARMBAND, IEquipmentTypeTag.EquipmentType.UNIFORM_TOP, IEquipmentTypeTag.EquipmentType.UNIFORM_PANTS);
        private final LinkedHashSet<IEquipmentTypeTag.EquipmentType> equipmentTypes;

        Tab(IEquipmentTypeTag.EquipmentType... equipmentTypes) {
            this.equipmentTypes = Arrays.stream(equipmentTypes).collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    @Override
    protected void init() {
        super.init();
        this.guiWidthMax = this.getGuiLeft() + imageWidth;
        this.guiHeightMax = this.getGuiTop() + imageHeight;
        this.submitButton = PlainTextButton.builder(
                        Component.translatable("gui.zerocontact.workbench.submit"), pressed -> onPressedSubmit()
                )
                .bounds(guiWidthMax - 42, guiHeightMax - 14, 36, 12)
                .build();

        submitButton.setAlpha(0);
        submitButton.active = false;
        submitButton.setFGColor(0x1bd60f);
        this.scrollList = new ScrollList(Minecraft.getInstance(), 152, 166, getGuiTop() + 22, getGuiTop() + 150, 36, this);
        scrollList.setLeftPos(getGuiLeft() + 12);
        buildTabButtons();
        addRenderableWidget(submitButton);
        addRenderableWidget(scrollList);
    }

    private void replaceData(Tab tab, BiConsumer<IEquipmentTypeTag.EquipmentType, List<GearRecipeData>> dataConsumer) {
        tab.equipmentTypes.forEach(type -> dataConsumer.accept(type, WorkBenchEntity.recipeData));
    }

    private void setCurrentTab(Tab tab) {
        LinkedHashSet<GearRecipeData> recipes = new LinkedHashSet<>();
        replaceData(tab, (type, data) -> {
            LinkedHashSet<GearRecipeData> filteredRecipes = data.stream().filter(e -> {
                Item gearItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(e.gearId));
                if (gearItem == null) return false;
                return gearItem instanceof IEquipmentTypeTag tag && tag.getArmorType().equals(type);
            }).collect(Collectors.toCollection(LinkedHashSet::new));
            recipes.addAll(filteredRecipes);
        });
        currentData.clear();
        currentData.addAll(recipes);
        addEntryToList();
    }

    private void buildTabButtons() {
        final int[] startX = {this.getGuiLeft()-2};
        final int startY = this.getGuiTop() -14;
        Arrays.stream(Tab.values()).toList().forEach(tab -> {
            MutableComponent mutableComponent = Component.translatable("gui.zerocontact.workbench." + tab.name().toLowerCase());
            Button button = Button.builder(mutableComponent, (btn) -> setCurrentTab(tab))
                    .bounds(startX[0], startY, 36, 12)
                    .build();

            button.setFGColor(0x1bd60f);
            addRenderableWidget(button);
            startX[0] += 36;
        });
    }

    private void addEntryToList() {
        scrollList.children().clear();
        scrollList.setScrollAmount(0);
        int recipeIndex = 0;
        for (GearRecipeData data : currentData) {
            scrollList.addGearEntry(new ScrollList.GearEntry(data, scrollList, recipeIndex));
            recipeIndex++;
        }
    }


    private void onPressedSubmit() {
        ScrollList.GearEntry entry = scrollList.getSelected();
        if (menu.blockEntity != null) {
            if (entry != null) {
                ModMessages.sendToServer(new NetworkHandler.BuyGearsPacket(menu.blockEntity.getBlockPos(), entry.gearItem));
            }
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (scrollList.getSelected() != null) {
            submitButton.active = true;
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

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        children().forEach(entry -> entry.mouseDragged(mouseX, mouseY, button, dragX, dragY));
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}
