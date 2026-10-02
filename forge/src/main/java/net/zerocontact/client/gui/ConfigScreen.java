package net.zerocontact.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.zerocontact.client.gui.components.ConfigOptionsList;
import net.zerocontact.config.ModConfigs;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ConfigScreen extends Screen {
    public static final String CONFIG_ZEROCONTACT_CLIENT_TRAJECTORY_TOOLTIP = "config.zerocontact.client.trajectory_tooltip";
    public static final String CONFIG_ZEROCONTACT_CLIENT_BULLET_SUPPRESSION = "config.zerocontact.client.bullet_suppression";
    public static final String CONFIG_ZEROCONTACT_CLIENT_AMMO_TYPE_OVERLAY = "config.zerocontact.client.ammo_type_overlay";
    public static final String CONFIG_ZEROCONTACT_CLIENT_AMMO_TYPE_TOOLTIP = "config.zerocontact.client.ammo_type_tooltip";
    public static final String CONFIG_ZEROCONTACT_CLIENT_AUDIO_EFFECT = "config.zerocontact.client.audio_effect";
    private final Screen parentScreen;

    public ConfigScreen(Component title, Screen parentScreen) {
        super(title);
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        int y = 48;
        ConfigOptionsList configOptionsList = new ConfigOptionsList(this, minecraft, width, height, y, height - 48, 32);
        ConfigOptionsList.Title clientCategory = new ConfigOptionsList.Title(Component.translatable("config.zerocontact.client"));
        configOptionsList.add(clientCategory);

        configOptionsList.registerBoolEntry(
                CONFIG_ZEROCONTACT_CLIENT_AUDIO_EFFECT,
                font,
                ModConfigs.CLIENT.audioEffect(),
                ModConfigs.CLIENT_CONFIG_SPEC
        );

        configOptionsList.registerBoolEntry(
                CONFIG_ZEROCONTACT_CLIENT_TRAJECTORY_TOOLTIP,
                font,
                ModConfigs.CLIENT.enableTrajectoryTooltip(),
                ModConfigs.CLIENT_CONFIG_SPEC
        );

        configOptionsList.registerBoolEntry(
                CONFIG_ZEROCONTACT_CLIENT_BULLET_SUPPRESSION,
                font,
                ModConfigs.CLIENT.enableBulletSuppression(),
                ModConfigs.CLIENT_CONFIG_SPEC
        );

        configOptionsList.registerBoolEntry(
                CONFIG_ZEROCONTACT_CLIENT_AMMO_TYPE_OVERLAY,
                font,
                ModConfigs.CLIENT.ammoTypeOverLay(),
                ModConfigs.CLIENT_CONFIG_SPEC
        );

        configOptionsList.registerBoolEntry(
                CONFIG_ZEROCONTACT_CLIENT_AMMO_TYPE_TOOLTIP,
                font,
                ModConfigs.CLIENT.ammoTypeTooltip(),
                ModConfigs.CLIENT_CONFIG_SPEC
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
