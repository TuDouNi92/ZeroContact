package net.zerocontact.client.interaction;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.cofig.ModConfigs;
import net.zerocontact.registries.ModSoundEventsReg;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BulletSuppression {
    private static float oldSuppression = 0.0f;
    private static float rollAmount = 0;
    private static float shakeAmount = 0;

    @SubscribeEvent
    public static void clientEntityTick(EntityEvent event) {
        tickBulletSuppression(event);
    }

    private static void tickBulletSuppression(EntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityKineticBullet) {
            BulletSuppression.apply(entity, Minecraft.getInstance().player, (bl) -> {
                bl.playSound(ModSoundEventsReg.randomBulletSound(), 2.0f, 1.0f);
                SuppressionManager.increaseSuppression(.1f);
            });
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!ModConfigs.CLIENT.enableBulletSuppression.get()) return;
        float suppression = SuppressionManager.suppressionLevel;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        double time = (level.getGameTime() + event.getPartialTick()) * 0.05f;

        float decayAmount = 0.00025f;
        if (suppression - oldSuppression > 0) {
            rollAmount = (float) Math.sin(time) * 1.6f * suppression;
            shakeAmount = (float) Math.cos(time) * 0.25f * suppression;
        } else {
            rollAmount = approachZero(rollAmount, decayAmount);
            shakeAmount = approachZero(shakeAmount, decayAmount);
        }
        event.setRoll(event.getRoll() + rollAmount);
        event.setPitch(event.getPitch() + shakeAmount);
        event.setYaw(event.getYaw() + shakeAmount);

        oldSuppression = suppression;
    }

    private static float approachZero(float value, float amount) {
        if (value > 0) {
            return Math.max(0, value - amount);
        }
        return Math.min(0, value + amount);
    }

    public static void apply(Entity bullet, @Nullable LivingEntity entity, Consumer<Entity> consumer) {
        if (!ModConfigs.CLIENT.enableBulletSuppression.get()) return;
        if (entity == null) return;
        if (bullet instanceof EntityKineticBullet) {
            Optional<Entity> bulletEntity = Optional.of(bullet);
            bulletEntity.ifPresent(bl -> {
                Vec3 targetPos = entity.getEyePosition();
                Vec3 relativePos = bullet.position().subtract(targetPos);
                Vec3 relativeVelocity = bullet.getDeltaMovement().subtract(entity.getDeltaMovement());
                double speedSqr = relativeVelocity.lengthSqr();
                if (speedSqr < 1.0E-8) {
                    return;
                }
                double closestTime = -relativePos.dot(relativeVelocity) / speedSqr;
                if (closestTime < 0.0 || closestTime > 1.0) {
                    return;
                }
                Vec3 closestOffset = relativePos.add(
                        relativeVelocity.scale(closestTime)
                );
                double closestDistance = closestOffset.length();

                if (closestDistance >= 1 && closestDistance <= 8) {
                    consumer.accept(bl);
                }
            });
        }
    }
}
