package net.zerocontact.datagen.model;

import com.google.gson.annotations.SerializedName;

public record ManifestPOJO(
        @SerializedName("pack_name")
        String tabName,
        String author,
        String version) {
}
