package net.zerocontact.neoforge;

import net.zerocontact.ZeroContactExpectPlatform;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ZeroContactExpectPlatformImpl {
    /**
     * This is our actual method to {@link ZeroContactExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
