package net.zerocontact.datagen;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.ZeroContact;
import net.zerocontact.caliber.CaliberVariantDamageHelper;
import net.zerocontact.caliber.HookEventTrigger;
import net.zerocontact.caliber.TargetSelector;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

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

    public float knockback = 0;

    //Camera recoil multiplier
    @SerializedName("recoil_multiplier")
    public float recoilMultiplier = 1;

    @SerializedName("inaccuracy_multiplier")
    public float inaccuracyMultiplier = 1;

    public Explosion explosion = Explosion.NONE;

    public Ignite ignite = Ignite.NONE;

    @SerializedName("base_damage_factor")
    //Indicates a damage balancing factor on guns with the same caliber.
    public float baseDamageFactor = 1;

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

    public record EventHook(
            HookEventTrigger trigger,
            List<HookActionData> actions,
            List<LuaHookData> scripts
    ) {
        public EventHook {
            actions = actions == null ? List.of() : List.copyOf(actions);
            scripts = scripts == null ? List.of() : List.copyOf(scripts);
        }

    }

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

    public record LuaHookData(
            String script,
            String function,
            Map<String, JsonElement> arguments
    ) {
        public LuaHookData {
            function = function == null ? "run" : function;
            arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        }
    }

}
