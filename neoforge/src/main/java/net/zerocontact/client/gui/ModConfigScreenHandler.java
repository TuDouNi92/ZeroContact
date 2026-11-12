package net.zerocontact.client.gui;

import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.zerocontact.ZeroContact;

@Mod(value = ZeroContact.MOD_ID, dist = Dist.CLIENT)
public class ModConfigScreenHandler {
    public ModConfigScreenHandler(ModContainer container) {
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (modContainer, parent) -> new ConfigScreen(Component.literal("Config screen"), parent)
        );
    }
}
