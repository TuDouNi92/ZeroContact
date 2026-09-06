package net.zerocontact.caliber.extension.model;

import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.datagen.model.AmmoDataPOJO;

import java.util.List;

public record HookEffectInvocation(AmmoDataPOJO.HookActionData data, HookContext context, List<LivingEntity> targets) {
}
