package net.zerocontact.compat;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.zerocontact.api.ICompatHandler;

import java.util.Optional;

public class MagazinesCompatHandler implements ICompatHandler {
    private final static String MOD_ID = "taczmagazines";
    private static MagazinesCompatHandler singleton;
    private MagazinesCompat compat;

    private MagazinesCompatHandler() {
    }

    public static MagazinesCompatHandler get() {
        if (singleton == null) {
            singleton = new MagazinesCompatHandler();
        }
        return singleton;
    }

    public Optional<MagazinesCompat> getCompat() {
        if (!isModLoaded()) return Optional.empty();
        if (compat == null) {
            compat = new MagazinesCompat();
        }
        return Optional.of(compat);
    }

    @Override
    public boolean foundInModList(String className) {
        boolean isOptionalLoaded = LoadingModList.get().getModFileById(MOD_ID) != null;
        if (className.endsWith("Mixin")) {
            return isOptionalLoaded;
        }
        return false;
    }

    @Override
    public boolean isModLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }
}
