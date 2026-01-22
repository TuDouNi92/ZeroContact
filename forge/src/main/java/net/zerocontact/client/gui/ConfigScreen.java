package net.zerocontact.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.zerocontact.cofig.ModConfigs;
import net.zerocontact.network.ModMessages;
import net.zerocontact.network.NetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ConfigScreen extends Screen {
    private final Screen parentScreen;

    public ConfigScreen(Component title, Screen parentScreen) {
        super(title);
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        int y = this.height / 4;
        addRenderableWidget(
                Button.builder(
                                Component.literal("Enable stamina: " + ModConfigs.COMMON.enableStamina.get()),
                                btn -> {
                                    boolean status = !ModConfigs.COMMON.enableStamina.get();
                                    ModConfigs.COMMON.enableStamina.set(status);
                                    Minecraft mc = Minecraft.getInstance();
                                    if (mc.getConnection() != null) {
                                        ModMessages.sendToServer(new NetworkHandler.ToggleStaminaPacket(status));
                                    }
                                    btn.setMessage(Component.literal("Enable stamina: " + status));
                                }
                        )
                        .bounds(this.width / 2 - 100, y, 200, 20)
                        .build()
        );

        addRenderableWidget(
                Button.builder(
                        CommonComponents.GUI_DONE,
                        btn -> Optional.ofNullable(this.minecraft).ifPresent(mc -> mc.setScreen(parentScreen))
                ).bounds(this.width / 2 - 100, y + 30, 200, 20).build()
        );
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
