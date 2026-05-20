package net.zerocontact.api;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

@ApiStatus.Internal
public interface IPackManager {

    void findOuterPacks() throws IOException;

    IAssetManager getAssetManager();

    /**
     * Generate and register packs from {@link #findOuterPacks()} ()}
     * */
    void registerVanillaDataPackBundle() throws IllegalArgumentException;

    /**
     * General endpoint for calling all necessary methods
     * */
    void init();

    /**
     * Generate default datapack from .zip file to configs
     * */
    void createDefaultPack() throws IOException;
    /**
     * Loads JSON from packs, should use a JSON {@code assetManager} which implemeted {@link IAssetManager} from the argument
     * */
    void loadOuterPack() throws IOException;
}
