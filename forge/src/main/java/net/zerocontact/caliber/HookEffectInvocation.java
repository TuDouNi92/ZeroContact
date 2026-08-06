package net.zerocontact.caliber;

import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.datagen.AmmoDataPOJO;

import java.util.List;

public record HookEffectInvocation(AmmoDataPOJO.HookActionData data, HookContext context, List<LivingEntity> targets) {
}
