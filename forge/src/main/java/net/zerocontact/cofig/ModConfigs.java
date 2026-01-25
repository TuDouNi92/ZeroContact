package net.zerocontact.cofig;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ModConfigs {
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final Common COMMON;
    static{
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder()
                .configure(Common::new);
        CONFIG_SPEC = pair.getRight();
        COMMON = pair.getLeft();
    }
    public static class Common{
        public final ForgeConfigSpec.BooleanValue enableStamina;
        Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            enableStamina = builder
                    .comment("Enable stamina")
                    .define("enableStamina", false);
            builder.pop();
        }
    }
}
