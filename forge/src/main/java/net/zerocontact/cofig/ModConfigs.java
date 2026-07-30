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
    public static final String ENABLE_BULLET_SUPPRESSION = "bulletSuppression";
    public static final String ENABLE_FLESH_ON_UNARMORED = "fleshDamageOnUnarmored";

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
            this(getEnabled(builder));
        }

        private static ForgeConfigSpec.BooleanValue getEnabled(ForgeConfigSpec.Builder builder) {
            final ForgeConfigSpec.BooleanValue enabledUniversalFleshDamage;
            builder.push("server");
            enabledUniversalFleshDamage = builder
                    .comment("Universal flesh damage")
                    .define(ENABLE_FLESH_ON_UNARMORED, true);
            builder.pop();
            return enabledUniversalFleshDamage;
        }
    }

    public record Client(ForgeConfigSpec.BooleanValue enableBulletSuppression) {
        Client(ForgeConfigSpec.Builder builder) {
            this(getEnabled(builder));
        }

        private static ForgeConfigSpec.BooleanValue getEnabled(ForgeConfigSpec.Builder builder) {
            final ForgeConfigSpec.BooleanValue enableBulletSuppression;
            builder.push("client");
            enableBulletSuppression = builder
                    .comment("Bullet suppression")
                    .define(ENABLE_BULLET_SUPPRESSION, true);
            builder.pop();
            return enableBulletSuppression;
        }
    }
}
