package net.zerocontact.datagen;

import java.nio.file.Path;

public record Zpack(
        String tab,
        Path outerPack,
        String author,
        String version
) {
}
