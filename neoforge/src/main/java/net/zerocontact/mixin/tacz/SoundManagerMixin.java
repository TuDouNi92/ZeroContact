package net.zerocontact.mixin.tacz;

import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageSound;
import com.tacz.guns.sound.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SoundManager.class, remap = false)
public class SoundManagerMixin {
    @Inject(
            method = "sendSoundToNearby",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tacz/guns/network/message/ServerMessageSound;<init>(ILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;FFI)V"
            ),
            cancellable = true)
    private static void sendSoundToNearby(LivingEntity sourceEntity, int distance, ResourceLocation gunId, ResourceLocation gunDisplayId, String soundName, float volume, float pitch, CallbackInfo ci) {
        var level = sourceEntity.level();
        if (!(level instanceof ServerLevel serverLevel)) return;
        double radius = 8 * 16;
        double radiusSqr = radius * radius;
        for (ServerPlayer player : serverLevel.players()) {
            if (player.getId() == sourceEntity.getId()) {
                continue;
            }
            if (player.distanceToSqr(sourceEntity.position()) >= radiusSqr) {
                continue;
            }
            ServerMessageSound soundMessage = new ServerMessageSound(sourceEntity.getId(), gunId, gunDisplayId, soundName, volume, pitch, distance);
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    soundMessage
            );
        }
        ci.cancel();
    }
}
