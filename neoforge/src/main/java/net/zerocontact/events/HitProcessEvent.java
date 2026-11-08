package net.zerocontact.events;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class HitProcessEvent extends Event {

    @Nullable protected final LivingEntity attacker;
    protected final Entity hurtEntity;
    protected final Entity bulletEntity;
    protected final DamageSource source;
    protected final EventArmorContext eventArmorContext;
    protected final EventAmmoContext eventAmmoContext;
    protected final ZHitOutcome outcome;
    protected final boolean headShot;
    protected float finalDamage;


    protected HitProcessEvent(@Nullable LivingEntity attacker, Entity hurtEntity, Entity bulletEntity, DamageSource source, EventArmorContext eventArmorContext, EventAmmoContext eventAmmoContext, ZHitOutcome outcome, boolean headShot, float finalDamage) {
        this.attacker = attacker;
        this.hurtEntity = hurtEntity;
        this.bulletEntity = bulletEntity;
        this.source = source;
        this.eventArmorContext = eventArmorContext;
        this.eventAmmoContext = eventAmmoContext;
        this.outcome = outcome;
        this.headShot = headShot;
        this.finalDamage = finalDamage;
    }

    public Entity getBulletEntity() {
        return bulletEntity;
    }

    public Entity getHurtEntity() {
        return hurtEntity;
    }

    public @Nullable LivingEntity getAttacker() {
        return attacker;
    }

    public EventArmorContext getEventArmorContext() {
        return eventArmorContext;
    }


    public EventAmmoContext getEventAmmoContext() {
        return eventAmmoContext;
    }

    public ZHitOutcome getOutcome() {
        return outcome;
    }

    public boolean isHeadShot() {
        return headShot;
    }

    public float getFinalDamage() {
        return finalDamage;
    }

    public record EventArmorContext(
            ItemStack armorStack,
            ItemStack plateStack,
            int armorProtectionLevel,
            int plateProtectionLevel
    ) {
        public EventArmorContext {
            armorStack = armorStack.copy();
            plateStack = plateStack.copy();
        }
    }

    public record EventAmmoContext(
            ResourceLocation ammoId,
            ResourceLocation ammoVariant,
            int ammoPenetration,
            float ammoFlesh,
            float armorDamage
    ) {
    }


    public enum ZHitOutcome {
        NON_PEN,
        PEN,
        NO_ARMOR,
        NO_OUTCOME,
        RICOCHET
    }

    @Cancelable
    public static class Pre extends HitProcessEvent {

        @ApiStatus.Internal
        public Pre(LivingEntity attacker, Entity hurtEntity, Entity bulletEntity, DamageSource source, EventArmorContext eventArmorContext, EventAmmoContext eventAmmoContext, ZHitOutcome outcome, boolean headShot, float finalDamage) {
            super(attacker, hurtEntity, bulletEntity, source, eventArmorContext, eventAmmoContext, outcome, headShot, finalDamage);
        }

        /**
         * The final damage has included headshot multiplier, penetration detection and mob damage factors.
         * Be aware of the damage process order before make changes.
         *
         * @param damage modified damage which is expected to take effect,it will get processed by TaCZ event right after.
         */
        public void setDamageAmount(float damage) {
            this.finalDamage = damage;
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    public static class Post extends HitProcessEvent {
        @ApiStatus.Internal
        public Post(LivingEntity attacker, LivingEntity hurtEntity, Entity bulletEntity, DamageSource source, EventArmorContext eventArmorContext, EventAmmoContext eventAmmoContext, ZHitOutcome outcome, boolean headShot, float finalDamage) {
            super(attacker, hurtEntity, bulletEntity, source, eventArmorContext, eventAmmoContext, outcome, headShot, finalDamage);
        }
    }
}
