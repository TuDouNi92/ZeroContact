package net.zerocontact.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.zerocontact.client.ClientData;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.client.menu.MySimpleContainerMenu;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class BackpackScreen extends AbstractContainerScreen<MySimpleContainerMenu> {
    public BackpackScreen(MySimpleContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(0, 0, width, height, 0, 0xaa435570);
        guiGraphics.fill(getGuiLeft(), getGuiTop(), getGuiLeft() + getXSize(), getGuiTop() + getYSize() + 16, 1, 0x88435570);
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
