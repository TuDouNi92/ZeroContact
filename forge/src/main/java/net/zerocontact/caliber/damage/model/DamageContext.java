package net.zerocontact.caliber.damage.model;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPart;
import org.jetbrains.annotations.NotNull;

public record DamageContext(
        EntityHurtByGunEvent.Pre event,
        @NotNull ItemStack plate,
        @NotNull ItemStack armor,
        @NotNull HitPart hitPart
) {
}
