package net.zerocontact.api.compat;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ICompatHandler {
    boolean foundInModList(String className);
    boolean isModLoaded();
}
