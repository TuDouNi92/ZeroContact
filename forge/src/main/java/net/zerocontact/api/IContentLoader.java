package net.zerocontact.api;

import net.zerocontact.datagen.Zpack;

import java.util.Set;

public interface IContentLoader {
    void load(Set<Zpack> packs);
}
