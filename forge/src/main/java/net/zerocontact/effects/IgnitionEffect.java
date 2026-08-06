package net.zerocontact.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.caliber.HookContext;
import net.zerocontact.caliber.HookEffectInvocation;
import net.zerocontact.datagen.AmmoDataPOJO;

import java.util.ArrayList;
import java.util.List;

public class IgnitionEffect extends MobEffect implements ZCEffect {
    public IgnitionEffect() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    @Override
    public void instantEffect(HookEffectInvocation hookEffectInvocation) {
        HookContext context = hookEffectInvocation.context();
        AmmoDataPOJO.HookActionData data = hookEffectInvocation.data();
            LivingEntity entity = context.shooter();
            if (entity == null) return;
            Level level = entity.level();
            if (level.isClientSide) return;
            Vec3 position = context.positon();
            BlockPos blockPos = BlockPos.containing(position.x, position.y, position.z);
            fillFire((ServerLevel) level, blockPos, data.radius());
            spawnExplosionParticles((ServerLevel) level,blockPos.getCenter(),data.radius());
    }

    @Override
    public void serverTickEffect(ServerLevel level) {

    }

    private void fillFire(
            ServerLevel level,
            BlockPos origin,
            float requestedRadius
    ) {
        float radius = Mth.clamp(requestedRadius, 0.0F, 32.0F);
        int range = Mth.ceil(radius);
        double radiusSqr = radius * radius;
        List<BlockPos> targets = new ArrayList<>();
        for (BlockPos cursor : BlockPos.betweenClosed(
                origin.offset(-range, -range, -range),
                origin.offset(range, range, range)
        )) {
            BlockPos pos = cursor.immutable();
            if (!level.isInWorldBounds(pos)) {
                continue;
            }
            // 把立方体过滤成球形
            if (pos.distSqr(origin) > radiusSqr) {
                continue;
            }
            if (!level.getBlockState(pos).canBeReplaced()) {
                continue;
            }
            BlockState fireState = BaseFireBlock.getState(level, pos);
            if (fireState.canSurvive(level, pos)) {
                targets.add(pos);
            }
        }
        // 先完成检查再统一修改世界，避免遍历过程中产生连锁判定
        for (BlockPos pos : targets) {
            if (!level.isEmptyBlock(pos)) {
                continue;
            }
            BlockState fireState = BaseFireBlock.getState(level, pos);
            if (fireState.canSurvive(level, pos)) {
                level.setBlockAndUpdate(pos, fireState);
            }
        }
    }

    private void spawnExplosionParticles(
            ServerLevel level,
            Vec3 center,
            float requestedRadius
    ) {
        float radius = Mth.clamp(requestedRadius, 0.5F, 32.0F);

        int particleCount = Mth.clamp(
                Mth.ceil(16.0F * radius),
                64,
                192
        );

        double goldenAngle = Math.PI * (3.0D - Math.sqrt(5.0D));
        double speed = 0.08D + radius * 0.025D;

        for (int i = 0; i < particleCount; i++) {
            double y = 1.0D - 2.0D * (i + 0.5D) / particleCount;
            double horizontal = Math.sqrt(1.0D - y * y);
            double angle = goldenAngle * i;

            double directionX = Math.cos(angle) * horizontal;
            double directionZ = Math.sin(angle) * horizontal;

            level.sendParticles(
                    ParticleTypes.FLAME,
                    center.x,
                    center.y,
                    center.z,
                    16,
                    directionX,
                    y,
                    directionZ,
                    speed
            );
        }
    }
}
