package net.zerocontact.api.datagen;

import net.zerocontact.datagen.model.Zpack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

@ApiStatus.Internal
public interface IContentLoader {
    void load(Set<Zpack> packs);
}
