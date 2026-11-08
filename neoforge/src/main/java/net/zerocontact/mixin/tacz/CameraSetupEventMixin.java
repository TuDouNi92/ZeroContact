package net.zerocontact.mixin.tacz;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.event.CameraSetupEvent;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunRecoil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.capability.CapabilityRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CameraSetupEvent.class)
public class CameraSetupEventMixin {

    @Redirect(
            method = "initialCameraRecoil",
            remap = false,
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/resource/pojo/data/gun/GunData;getRecoil()Lcom/tacz/guns/resource/pojo/data/gun/GunRecoil;")
    )
    private static GunRecoil modifyRecoil(GunData gunData) {
        GunRecoil original = gunData.getRecoil();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return original;
        ItemStack gunStack = player.getMainHandItem();
        if (!(gunStack.getItem() instanceof IGun)) return original;
        return gunStack.getCapability(CapabilityRegistries.CARTRIDGE).map(cap -> {
            GunRecoil recoil = cap.getRecoil(gunStack);
            if (recoil == null) return original;
            return recoil;
        }).orElse(original);
    }
}
