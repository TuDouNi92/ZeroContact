package net.zerocontact.datagen.loader;

import net.zerocontact.api.IAssetManager;
import net.zerocontact.api.IContentLoader;
import net.zerocontact.datagen.ExperimentalBallisticData;
import net.zerocontact.datagen.GearRecipeData;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.Zpack;
import net.zerocontact.events.CaliberVariantDamageHelper;
import net.zerocontact.item.block.WorkBenchEntity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ZContentLoader implements IContentLoader {
    public static final LinkedHashMap<Object, String> itemGenData = new LinkedHashMap<>();
    private final IAssetManager assetManager;
    private static final String DEFAULT_RECIPE_NAME = "default.json";
    private static final String ITEM_PATH = "data/" + MOD_ID + "/items";
    private static final String AMMO_DEF_PATH = "data/" + MOD_ID + "/ammoDefinitions";
    private static final String RECIPES_PATH = "data/" + MOD_ID + "/gear_recipes";

    public ZContentLoader(IAssetManager assetManager) {
        this.assetManager = assetManager;
    }


    @Override
    public void loadItems(Set<Zpack> packs) {
        packs.forEach(pack -> {
            Path itemPath = pack.outerPack().resolve(ITEM_PATH);
            try {
                List<Path> itemList = assetManager.getJsonListPathsFromPath(itemPath);
                assetManager.deserializeFromJsonList(itemList, assetManager.getGson(), ItemGenData.class, (data, __) -> itemGenData.put(data, pack.tab()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void loadBallistics(Set<Zpack> packs) {
        packs.forEach(pack -> {
            Path ballisticPath = pack.outerPack().resolve(AMMO_DEF_PATH);
            try {
                List<Path> ammoList = assetManager.getJsonListPathsFromPath(ballisticPath);
                assetManager.deserializeFromJsonList(
                        ammoList,
                        assetManager.getGson(),
                        ExperimentalBallisticData.class,
                        (data, __) -> {
                            itemGenData.put(data, pack.tab());
                            CaliberVariantDamageHelper.experimentalBallisticSet.
                                    add(
                                            new CaliberVariantDamageHelper.Caliber(
                                                    data.ammoId,
                                                    data.variant,
                                                    data.baseDamageFactor,
                                                    data.penetrationClass,
                                                    data.fleshDamage,
                                                    data.armorDamage,
                                                    data.stackSize,
                                                    data.tracerColor
                                            )
                                    );
                        }
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void loadRecipes(Set<Zpack> packs) {
        Map<String, List<GearRecipeData.IngredientItems>> merged = new HashMap<>();
        for (Zpack pack : packs) {
            Path recipesPath = pack.outerPack().resolve(RECIPES_PATH);
            try {
                List<Path> recipePaths = assetManager.getJsonListPathsFromPath(recipesPath);
                assetManager.deserializeFromJsonList(
                        recipePaths,
                        assetManager.getGson(),
                        GearRecipeData.class,
                        (data, path) -> {
                            if (data == null || data.recipes == null) return;
                            boolean isDefault = path.getFileName().toString().equals(DEFAULT_RECIPE_NAME);
                            for (GearRecipeData recipe : data.recipes) {
                                if (isDefault) {
                                    merged.putIfAbsent(recipe.gearId, recipe.ingredientItems);
                                } else {
                                    merged.put(recipe.gearId, recipe.ingredientItems);
                                }
                            }
                        }
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        WorkBenchEntity.recipeData = merged.entrySet().stream()
                .map(e -> new GearRecipeData(e.getKey(), e.getValue()))
                .toList();
    }
}
