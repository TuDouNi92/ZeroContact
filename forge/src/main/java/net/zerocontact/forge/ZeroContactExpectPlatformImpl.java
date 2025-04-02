package net.zerocontact.forge;

import net.zerocontact.ZeroContactExpectPlatform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ZeroContactExpectPlatformImpl {
    /**
     * This is our actual method to {@link ZeroContactExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
