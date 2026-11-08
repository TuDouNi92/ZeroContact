package net.zerocontact.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModLoadingContext;

public class ModConfigScreenHandler {
    public static void register(ModLoadingContext context){
        context.registerExtensionPoint(
                net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> new ConfigScreen(Component.literal("Config screen"), parent)
                )
        );
    }
}
