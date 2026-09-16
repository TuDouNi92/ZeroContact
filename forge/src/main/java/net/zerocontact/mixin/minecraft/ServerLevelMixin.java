package net.zerocontact.mixin.minecraft;

import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @ModifyArg(
            method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcast(Lnet/minecraft/world/entity/player/Player;DDDDLnet/minecraft/resources/ResourceKey;Lnet/minecraft/network/protocol/Packet;)V"
            ),
            index = 4
    )
    private double zerocontact$expandEntitySoundRadius(
            @Nullable Player except,
            double x, double y, double z,
            double originalRadius,
            ResourceKey<Level> dimension,
            Packet<?> packet) {
        return originalRadius * 2;
    }
    @ModifyArg(
            method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcast(Lnet/minecraft/world/entity/player/Player;DDDDLnet/minecraft/resources/ResourceKey;Lnet/minecraft/network/protocol/Packet;)V"
            ),
            index = 4
    )
    private double zerocontact$expandSoundRadius(
            @Nullable Player except,
            double x, double y, double z,
            double originalRadius,
            ResourceKey<Level> dimension,
            Packet<?> packet) {
        return originalRadius * 2;
    }
}
