package net.zerocontact.caliber;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public record HookActionData(
        TargetSelector target,
        String effect,
        int duration,
        int amplifier,
        float chance,
        float radius
) {
    public @Nullable MobEffect resolveEffect() {
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effect));
    }
}
