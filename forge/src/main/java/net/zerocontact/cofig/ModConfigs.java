package net.zerocontact.cofig;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ModConfigs {
    public static final ForgeConfigSpec CLIENT_CONFIG_SPEC;
    public static final ForgeConfigSpec COMMON_CONFIG_SPEC;
    public static final Common COMMON;
    public static final Client CLIENT;
    public static final String ENABLE_STAMINA = "enableStamina";
    public static final String ENABLE_BULLET_SOUND = "playBulletSound";

    static {
        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder()
                .configure(Client::new);
        Pair<Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder()
                .configure(Common::new);
        COMMON_CONFIG_SPEC = commonPair.getRight();
        CLIENT_CONFIG_SPEC = clientPair.getRight();
        COMMON = commonPair.getLeft();
        CLIENT = clientPair.getLeft();
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

    public static class Client {
        public final ForgeConfigSpec.BooleanValue playBulletSound;

        Client(ForgeConfigSpec.Builder builder) {
            builder.push("client");
            playBulletSound = builder
                    .comment("Play bullet whizz sound")
                    .define(ENABLE_BULLET_SOUND, false);
            builder.pop();
        }
    }
}
