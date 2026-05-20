package net.zerocontact.api;

import net.zerocontact.datagen.Zpack;

import java.util.Set;

public interface IContentLoader {
    default void load(Set<Zpack> packs){
        loadItems(packs);
        loadBallistics(packs);
        loadRecipes(packs);
    }
    void loadItems(Set<Zpack> packs);
    void loadBallistics(Set<Zpack> packs);
    void loadRecipes(Set<Zpack> packs);
}
