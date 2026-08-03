package net.zerocontact.mixin.tacz;

import com.tacz.guns.entity.EntityKineticBullet;
import net.zerocontact.caliber.CaliberVariantDamageHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityKineticBullet.class, remap = false)
public interface EntityKineticBulletAccessor {
    @Accessor("life")
    int life();

    @Accessor("life")
    void life(int ticks);

    @Accessor("speed")
    float speed();

    @Accessor("speed")
    void speed(float speed);

    @Accessor("friction")
    float friction();

    @Accessor("friction")
    void friction(float amount);

    @Accessor("gravity")
    float gravity();

    @Accessor("gravity")
    void gravity(float gravity);

    @Accessor("knockback")
    float knockback();

    @Accessor("knockback")
    void knockback(float amount);

    @Accessor("explosion")
    boolean explosion();

    @Accessor("explosion")
    void explosion(boolean explosion);

    @Accessor("explosionKnockback")
    boolean explosionKnockback();

    @Accessor("explosionKnockback")
    void explosionKnockback(boolean knockback);

    @Accessor("explosionDestroyBlock")
    boolean explosionDestroyBlock();

    @Accessor("explosionDestroyBlock")
    void explosionDestroyBlock(boolean destroy);

    @Accessor("explosionRadius")
    float explosionRadius();

    @Accessor("explosionRadius")
    void explosionRadius(float radius);

    @Accessor("explosionDamage")
    float explosionDamage();

    @Accessor("explosionDamage")
    void explosionDamage(float damage);

    @Accessor("explosionDelayCount")
    int explosionDelayCount();

    @Accessor("explosionDelayCount")
    void explosionDelayCount(int ticks);

    @Accessor("igniteEntity")
    boolean igniteEntity();

    @Accessor("igniteEntity")
    void igniteEntity(boolean ignite);

    @Accessor("igniteEntityTime")
    int igniteEntityTime();

    @Accessor("igniteEntityTime")
    void igniteEntityTime(int secs);

    @Accessor("igniteBlock")
    boolean igniteBlock();

    @Accessor("igniteBlock")
    void igniteBlock(boolean ignite);
}
