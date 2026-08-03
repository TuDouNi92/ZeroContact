package net.zerocontact.datagen;

import com.google.gson.annotations.SerializedName;
import net.zerocontact.ZeroContact;
import net.zerocontact.caliber.CaliberVariantDamageHelper;
import net.zerocontact.caliber.EventHook;

public class AmmoDataPOJO {
    @SerializedName("ammo_id")
    //The identifier of a type of ammo that has been registered in TACZ gun pack.
    public String ammoId;

    //The variant cartridge of this ammo, for example {hp}, any name you want.
    public String variant;

    //Lifecycle of a spawned bullet
    public int life = 30;

    //Deprecated(?
    public float speed = 1;

    //Air friction, affects reduction of the velocity
    public float friction = 0.015f;

    //Affects the ballistics
    public float gravity = 0.15f;

    public float knockback;

    //Camera recoil multiplier
    @SerializedName("recoil_multiplier")
    public float recoilMultiplier = 1;

    @SerializedName("inaccuracy_multiplier")
    public float inaccuracyMultiplier = 1;

    public Explosion explosion = Explosion.NONE;

    public Ignite ignite = Ignite.NONE;

    @SerializedName("base_damage_factor")
    //Indicates a damage balancing factor on guns with the same caliber.
    public float baseDamageFactor;

    //The penetration against armors.
    @SerializedName("penetration_class")
    public int penetrationClass = 10;
    @SerializedName("flesh_damage")
    //Flesh damage indicates the damage being produced through direct hit on penetrated armor, or direct hit on bodies.
    public float fleshDamage = 4;

    //The percent of consumed armor durability. Leaving {0} means a default process.
    @SerializedName("armor_damage")
    public float armorDamage = 0;

    @SerializedName("stack_size")
    public int stackSize = 30;

    //The color of this bullet tracer.
    @SerializedName("tracer_color")
    public int[] tracerColor = new int[]{255, 255, 255, 255};

    public EventHook[] effects = new EventHook[]{};

    public CaliberVariantDamageHelper.Caliber toCaliber() {
        return new CaliberVariantDamageHelper.Caliber(
                ammoId,
                ZeroContact.MOD_ID + ":" + variant,
                life,
                speed,
                friction,
                gravity,
                knockback,
                recoilMultiplier,
                inaccuracyMultiplier,
                explosion,
                ignite,
                baseDamageFactor,
                penetrationClass,
                fleshDamage,
                armorDamage,
                stackSize,
                tracerColor,
                effects
        );
    }

    public record Explosion(
            float radius,
            float damage,
            @SerializedName("destroy_block")
            boolean destroyBlock,
            boolean knockback,
            @SerializedName("delay_count")
            int delayCount
    ) {
        public static final Explosion NONE = new Explosion(0, 0, false, false, 0);
    }

    public record Ignite(
            @SerializedName("ignite_block")
            boolean igniteBlock,
            @SerializedName("ignite_entity")
            boolean igniteEntity,
            @SerializedName("ignite_entity_time")
            int igniteEntityTime
    ) {
        public static final Ignite NONE = new Ignite(false, false, 0);
    }
}
