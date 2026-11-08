package net.zerocontact.datagen.model;

import java.nio.file.Path;

public record Zpack(
        String tab,
        Path outerPack,
        String author,
        String version
) {
}
