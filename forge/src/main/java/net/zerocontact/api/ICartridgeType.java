package net.zerocontact.api;

import com.tacz.guns.resource.pojo.data.gun.GunRecoil;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ICartridgeType {
    @NotNull Map<InaccuracyType, Float> getInaccuracy(ItemStack gunStack);
    @Nullable GunRecoil getRecoil(ItemStack gunStack);
}
