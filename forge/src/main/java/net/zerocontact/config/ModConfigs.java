package net.zerocontact.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ModConfigs {
    public static final ForgeConfigSpec CLIENT_CONFIG_SPEC;
    public static final ForgeConfigSpec COMMON_CONFIG_SPEC;
    public static final ForgeConfigSpec SERVER_CONFIG_SPEC;
    public static final Common COMMON;
    public static final Client CLIENT;
    public static final Server SERVER;

    static {
        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder()
                .configure(Client::new);
        Pair<Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder()
                .configure(Common::new);
        Pair<Server, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder()
                .configure(Server::new);
        COMMON_CONFIG_SPEC = commonPair.getRight();
        CLIENT_CONFIG_SPEC = clientPair.getRight();
        SERVER_CONFIG_SPEC = serverPair.getRight();
        COMMON = commonPair.getLeft();
        CLIENT = clientPair.getLeft();
        SERVER = serverPair.getLeft();
    }

    public static void flipValue(ForgeConfigSpec.BooleanValue booleanValue) {
        booleanValue.set(!booleanValue.get());
    }

    public static class Common {


        Common(ForgeConfigSpec.Builder builder) {

        }
    }

    public record Server(
            ForgeConfigSpec.BooleanValue enableUniversalFleshDamage,
            ForgeConfigSpec.DoubleValue firstAidLimbsFactor,
            ForgeConfigSpec.DoubleValue firstAidHeadFactor
    ) {

        public static final String DAMAGE_CAT = "damage";

        public static final String FIRST_AID_CAT = "first_aid";

        public static final String FLESH_ON_UNARMORED = "flesh_damage_on_unarmored";
        public static final String LIMBS_FACTOR = "limbs_factor";
        private static final String HEAD_FACTOR = "head_factor";

        public static final String UNIVERSAL_FLESH_DAMAGE_COM = "Universal flesh damage";
        public static final String DAMAGE_FACTOR_FOR_LIMBS_COM = "Adjust the damage factor for limbs";
        private static final String DAMAGE_FACTOR_FOR_HEAD_COM = "Adjust the damage factor for head";



        Server(ForgeConfigSpec.Builder builder) {
            this(
                    buildBoolConfig(builder, DAMAGE_CAT, UNIVERSAL_FLESH_DAMAGE_COM, FLESH_ON_UNARMORED, true),
                    buildDoubleConfig(builder, FIRST_AID_CAT, DAMAGE_FACTOR_FOR_LIMBS_COM, LIMBS_FACTOR,0.25d),
                    buildDoubleConfig(builder, FIRST_AID_CAT, DAMAGE_FACTOR_FOR_HEAD_COM, HEAD_FACTOR,0.2d)
            );
        }
    }

    public record Client(
            ForgeConfigSpec.BooleanValue audioEffect,
            ForgeConfigSpec.BooleanValue enableBulletSuppression,
            ForgeConfigSpec.BooleanValue enableTrajectoryTooltip,
            ForgeConfigSpec.BooleanValue ammoTypeOverLay,
            ForgeConfigSpec.BooleanValue ammoTypeTooltip
            ) {

        public static final String TOOLTIP_CAT = "tooltips";
        public static final String SOUND_EFFECTS_VISUAL_EFFECTS_CAT = "sound_and_visual_effects";

        public static final String BULLET_SUPPRESSION = "bullet_suppression";
        public static final String TRAJECTORY_TOOLTIP = "trajectory_tooltip";
        public static final String AMMO_TYPE_OVERLAY = "ammo_type_Overlay";
        public static final String AMMO_TYPE_TOOLTIP = "ammo_type_toolTip";
        public static final String AUDIO_EFFECT = "audio_effect";

        public static final String TRAJECTORY_TOOLTIP_COM = "Shows trajectory while holding the gun and inspect inventory ammo";
        public static final String AMMO_TYPE_OVERLAY_COM = "Shows ammo type overlay";
        public static final String AMMO_TYPE_TOOLTIP_COM = "Shows ammo type tooltip";
        public static final String ENHANCED_AUDIO_EFFECT_COM = "Enhance the gunfire so the Headset lowering noises work as expectedly";
        public static final String BULLET_SUPPRESSION_EFFECT_COM = "React to incoming bullets by visual and sounds";

        Client(ForgeConfigSpec.Builder builder) {
            this(
                    buildBoolConfig(builder, SOUND_EFFECTS_VISUAL_EFFECTS_CAT, ENHANCED_AUDIO_EFFECT_COM, AUDIO_EFFECT,true),
                    buildBoolConfig(builder, SOUND_EFFECTS_VISUAL_EFFECTS_CAT, BULLET_SUPPRESSION_EFFECT_COM, BULLET_SUPPRESSION, true),
                    buildBoolConfig(builder, TOOLTIP_CAT, TRAJECTORY_TOOLTIP_COM, TRAJECTORY_TOOLTIP, true),
                    buildBoolConfig(builder, TOOLTIP_CAT, AMMO_TYPE_OVERLAY_COM, AMMO_TYPE_OVERLAY, true),
                    buildBoolConfig(builder, TOOLTIP_CAT, AMMO_TYPE_TOOLTIP_COM, AMMO_TYPE_TOOLTIP, true)
            );
        }
    }

    private static ForgeConfigSpec.BooleanValue buildBoolConfig(ForgeConfigSpec.Builder builder, String categoryName, String comment, String path, boolean defaultValue) {
        final ForgeConfigSpec.BooleanValue value;
        builder.push(categoryName);
        value = builder
                .comment(comment)
                .define(path, defaultValue);
        builder.pop();
        return value;
    }

    private static ForgeConfigSpec.DoubleValue buildDoubleConfig(ForgeConfigSpec.Builder builder, String categoryName, String comment, String path, double defaultValue) {
        final ForgeConfigSpec.DoubleValue value;
        builder.push(categoryName);
        value =  builder
                .comment(comment)
                .defineInRange(path, defaultValue,0.1,128);
        builder.pop();
        return value;
    }
}
