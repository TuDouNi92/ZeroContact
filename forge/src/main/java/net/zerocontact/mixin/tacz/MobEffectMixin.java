package net.zerocontact.mixin.tacz;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.zerocontact.caliber.HookEffectInvocation;
import net.zerocontact.effects.ZCEffect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MobEffect.class)
public class MobEffectMixin implements ZCEffect {
    @Override
    public void instantEffect(HookEffectInvocation hookEffectInvocation) {

    }

    @Override
    public void serverTickEffect(ServerLevel level) {

    }
}
