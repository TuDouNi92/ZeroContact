package net.zerocontact.api;

public interface ICompatHandler {
    boolean foundInModList(String className);
    boolean isModLoaded();
}
