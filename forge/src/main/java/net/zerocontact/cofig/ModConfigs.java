package net.zerocontact.cofig;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ModConfigs {
    public static final ForgeConfigSpec CLIENT_CONFIG_SPEC;
    public static final ForgeConfigSpec COMMON_CONFIG_SPEC;
    public static final ForgeConfigSpec SERVER_CONFIG_SPEC;
    public static final Common COMMON;
    public static final Client CLIENT;
    public static final Server SERVER;
    public static final String ENABLE_STAMINA = "enableStamina";

    public static final String CLIENT_SIDE = "client";
    public static final String SERVER_SIDE = "server";

    public static final String BULLET_SUPPRESSION = "bulletSuppression";
    public static final String FLESH_ON_UNARMORED = "fleshDamageOnUnarmored";
    public static final String TRAJECTORY_TOOLTIP = "trajectoryTooltip";
    public static final String AMMO_TYPE_OVERLAY = "ammoTypeOverlay";
    public static final String AMMO_TYPE_TOOLTIP = "ammoTypeToolTip";

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
        public final ForgeConfigSpec.BooleanValue enableStamina;

        Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            enableStamina = builder
                    .comment("Enable stamina")
                    .define(ENABLE_STAMINA, false);
            builder.pop();
        }
    }

    public record Server(ForgeConfigSpec.BooleanValue enableUniversalFleshDamage) {
        Server(ForgeConfigSpec.Builder builder) {
            this(getBuiltValue(builder, SERVER_SIDE, "Universal flesh damage", FLESH_ON_UNARMORED, true));
        }
    }

    public record Client(
            ForgeConfigSpec.BooleanValue enableBulletSuppression,
            ForgeConfigSpec.BooleanValue enableTrajectoryTooltip,
            ForgeConfigSpec.BooleanValue ammoTypeOverLay,
            ForgeConfigSpec.BooleanValue ammoTypeTooltip
            ) {
        Client(ForgeConfigSpec.Builder builder) {
            this(
                    getBuiltValue(builder, CLIENT_SIDE, "Bullet suppression", BULLET_SUPPRESSION, true),
                    getBuiltValue(builder, CLIENT_SIDE, "Trajectory tooltip", TRAJECTORY_TOOLTIP, true),
                    getBuiltValue(builder, CLIENT_SIDE, "Ammo type overlay", AMMO_TYPE_OVERLAY, true),
                    getBuiltValue(builder, CLIENT_SIDE, "Ammo type tooltip", AMMO_TYPE_TOOLTIP, true)
            );
        }
    }

    private static ForgeConfigSpec.BooleanValue getBuiltValue(ForgeConfigSpec.Builder builder, String side, String comment, String path, boolean defaultValue) {
        final ForgeConfigSpec.BooleanValue value;
        builder.push(side);
        value = builder
                .comment(comment)
                .define(path, true);
        return value;
    }
}
