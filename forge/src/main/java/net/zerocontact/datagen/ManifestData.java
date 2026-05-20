package net.zerocontact.datagen;

import com.google.gson.annotations.SerializedName;

public record ManifestData(
        @SerializedName("pack_name")
        String tabName,
        String author,
        String version) {
}
