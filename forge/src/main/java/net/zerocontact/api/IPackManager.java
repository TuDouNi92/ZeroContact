package net.zerocontact.api;

import com.google.gson.Gson;
import net.minecraft.server.packs.repository.Pack;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Set;

@ApiStatus.Internal
public interface IPackManager {

    /**
     * Get GSON object that may include type factories
     * */
    Gson getGson();

    /**
     * Get the path of the custom pack from where it should be
     * */
    Path getPacksPath() throws InvalidPathException;

    /**
     * Get packs which should be generated into resource packs and datapacks
     * */
    Set<Path> getOuterPacks();

    IAssetManager getAssetManager();

    /**
     * Generate and register packs from {@link #getOuterPacks()}
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
    <T extends IAssetManager> void loadOuterPack(T assetManager) throws IOException;
}
