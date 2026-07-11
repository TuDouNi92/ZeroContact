package net.zerocontact.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.zerocontact.api.ICompatHandler;

public class MagazinesCompatHandler implements ICompatHandler {
    private final static String MOD_ID = "taczmagazines";
    private static MagazinesCompatHandler singleton;

    private MagazinesCompatHandler() {
    }

    public static MagazinesCompatHandler getInstance() {
        if (singleton == null) {
            singleton = new MagazinesCompatHandler();
        }
        return singleton;
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

    public boolean isMagazineCompatibleWithGun(ItemStack gunStack) {
        if(!isModLoaded())return false;
        return MagazinesCompat.isMagazineCompatibleWithGun(gunStack);
    }

    public ItemStack getCompatibleMag(ItemStack gunStack) {
        if(!isModLoaded())return ItemStack.EMPTY;
        return MagazinesCompat.getCompatibleMag(gunStack);
    }
}
