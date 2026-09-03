package net.zerocontact.datagen;

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
        public Pattern {
            headshotMultiplier = headshotMultiplier == 0.0f ? 1 : headshotMultiplier;
            bodyshotMultiplier = bodyshotMultiplier == 0.0f ? 1 : bodyshotMultiplier;
        }
    }
}
