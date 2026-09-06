package net.zerocontact.datagen.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MobRulesPOJO {
    public List<Pattern> mobs;

    public record Pattern(
            @SerializedName("mob_id")
            String mobId,
            @SerializedName("headshot_multiplier")
            float headshotMultiplier,
            @SerializedName("bodyshot_multiplier")
            float bodyshotMultiplier
    ) {
    }
}
