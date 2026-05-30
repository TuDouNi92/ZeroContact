package net.zerocontact.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.client.menu.AmmoSelectorMenu;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;
import org.jetbrains.annotations.NotNull;

public class AmmoSelectorScreen extends AbstractContainerScreen<AmmoSelectorMenu> {
    private final Minecraft mc = Minecraft.getInstance();
    private final AmmoSelectorRenderUtil.Ring ring = new AmmoSelectorRenderUtil.Ring(
            (float) mc.getWindow().getGuiScaledWidth() / 2,
            (float) mc.getWindow().getGuiScaledHeight() / 2,
            64,
            96,
            menu.ammo.size(),
            4f
    );

    public AmmoSelectorScreen(AmmoSelectorMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderSelector(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

    }

    public void renderSelector(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (mc.player == null) return;
        if (mc.isPaused()) return;
        AmmoSelectorRenderUtil.drawSegmentedRing(
                guiGraphics,
                ring,
                16,
                0x55000000
        );
        renderAmmoLabel(ring, guiGraphics);
        renderHighLight(guiGraphics, mouseX, mouseY, ring);
    }

    private static void renderHighLight(GuiGraphics guiGraphics, int mouseX, int mouseY, AmmoSelectorRenderUtil.Ring ring) {
        int index = AmmoSelectorRenderUtil.getHoveredSegment(mouseX, mouseY, ring);
        if (AmmoSelectorRenderUtil.getHoveredSegment(mouseX, mouseY, ring) != -1) {
            AmmoSelectorRenderUtil.drawSegmentedRingPart(guiGraphics, ring, index, 16, 0x33FFFFFF);
        }
    }

    public void renderAmmoLabel(AmmoSelectorRenderUtil.Ring ring, GuiGraphics guiGraphics) {
        float centerRadius = (ring.outerRadius() + ring.innerRadius()) / 2;
        for (int i = 0; i < ring.segmentCount(); i++) {
            double sectionAngle = 2.0 * Math.PI / ring.segmentCount();
            double baseAngle = -Math.PI / 2.0;
            double angle = baseAngle + sectionAngle * i + sectionAngle / 2.0;
            int x = (int) Math.floor(ring.centerX() + Math.cos(angle) * centerRadius);
            int y = (int) Math.floor(ring.centerY() + Math.sin(angle) * centerRadius);
            ItemStack stack = menu.ammo.get(i).getKey();
            Integer count = menu.ammo.get(i).getValue();
            guiGraphics.renderFakeItem(stack, x - 8, y - 8);
            guiGraphics.drawString(font, count.toString(), x - font.width(count.toString()) / 2, y + 16, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int ammoIndex = AmmoSelectorRenderUtil.getHoveredSegment(mouseX, mouseY,ring);
            if(ammoIndex ==-1)return true;
            ItemStack ammoItem = menu.ammo.get(ammoIndex).getKey();
            ModMessages.sendToServer(new NetworkHandler.SelectAmmoPacket(ammoItem));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (KeyBindingHandler.TOGGLE_AMMO_SELECTOR.matches(keyCode, scanCode)) {
            onClose();
        }
        return false;
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }
}
