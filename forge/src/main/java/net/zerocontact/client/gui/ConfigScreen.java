package net.zerocontact.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.zerocontact.client.gui.components.ConfigOptionsList;
import net.zerocontact.cofig.ModConfigs;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ConfigScreen extends Screen {
    private final Screen parentScreen;
    private ConfigOptionsList configOptionsList;

    public ConfigScreen(Component title, Screen parentScreen) {
        super(title);
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        int y = 48;
        this.configOptionsList = new ConfigOptionsList(this, minecraft, width, height, y, height - 48, 32);
        ConfigOptionsList.Title clientCategory = new ConfigOptionsList.Title(Component.translatable("config.zerocontact.client"));
        ConfigOptionsList.Title serverCategory = new ConfigOptionsList.Title(Component.translatable("config.zerocontact.server"));
        configOptionsList.add(clientCategory);
        configOptionsList.registerBoolEntry(
                "config.zerocontact.client.trajectory_tooltip",
                font,
                ModConfigs.CLIENT.enableTrajectoryTooltip(),
                ModConfigs.CLIENT_CONFIG_SPEC
        );

        configOptionsList.registerBoolEntry(
                "config.zerocontact.client.bullet_suppression",
                font,
                ModConfigs.CLIENT.enableBulletSuppression(),
                ModConfigs.CLIENT_CONFIG_SPEC
        );

        configOptionsList.registerBoolEntry(
                "config.zerocontact.client.ammo_type_overlay",
                font,
                ModConfigs.CLIENT.ammoTypeOverLay(),
                ModConfigs.CLIENT_CONFIG_SPEC
        );

        configOptionsList.registerBoolEntry(
                "config.zerocontact.client.ammo_type_tooltip",
                font,
                ModConfigs.CLIENT.ammoTypeTooltip(),
                ModConfigs.CLIENT_CONFIG_SPEC
        );
        configOptionsList.add(serverCategory);

        configOptionsList.registerBoolEntry(
                "config.zerocontact.server.universal_flesh",
                font,
                ModConfigs.SERVER.enableUniversalFleshDamage(),
                ModConfigs.SERVER_CONFIG_SPEC
        );

        addRenderableWidget(configOptionsList);
        Button submitButton = Button.builder(
                CommonComponents.GUI_DONE,
                btn -> Optional.ofNullable(this.minecraft).ifPresent(mc -> mc.setScreen(parentScreen))
        ).bounds(this.width / 2 - 100, this.height - 36, 200, 20).build();
        addRenderableWidget(
                submitButton
        );
    }


    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
