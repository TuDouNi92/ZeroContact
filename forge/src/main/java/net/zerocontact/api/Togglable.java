package net.zerocontact.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public interface Togglable {
     boolean isVisor();
     Item getToggleBrother();
     ResourceLocation getVisorTexture();
}
