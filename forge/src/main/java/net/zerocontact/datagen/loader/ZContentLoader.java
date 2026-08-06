package net.zerocontact.datagen.loader;

import com.google.gson.JsonSyntaxException;
import net.zerocontact.api.IAssetManager;
import net.zerocontact.api.IContentLoader;
import net.zerocontact.caliber.CaliberRegistry;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.datagen.AmmoDataPOJO;
import net.zerocontact.datagen.GearRecipeData;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.Zpack;
import net.zerocontact.item.block.WorkBenchEntity;
import net.zerocontact.lua.ZCLuaEngine;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ZContentLoader implements IContentLoader {
    public static final LinkedHashMap<Object, String> itemGenData = new LinkedHashMap<>();
    private final IAssetManager assetManager;
    private static final String DEFAULT_RECIPE_NAME = "default.json";
    private static final String ITEM_PATH = "data/" + MOD_ID + "/items";
    private static final String AMMO_DEF_PATH = "data/" + MOD_ID + "/ammoDefinitions";
    private static final String AMMO_SCRIPT_PATH = "data/" + MOD_ID + "/scripts";
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
                ZeroContactLogger.LOG.error("Failed to load item data: ", e);
            } catch (JsonSyntaxException jsonSyntaxException) {
                ZeroContactLogger.LOG.error("Failed to parse json data: ", jsonSyntaxException);
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
                        AmmoDataPOJO.class,
                        (data, __) -> {
                            itemGenData.put(data, pack.tab());
                            CaliberRegistry.register(data.toCaliber());
                        }
                );
            } catch (IOException e) {
                ZeroContactLogger.LOG.error("Failed to read ammo data: ", e);
            } catch (JsonSyntaxException jsonSyntaxException) {
                ZeroContactLogger.LOG.error("Failed to parse json data: ", jsonSyntaxException);
            }
        });
    }

    @Override
    public void loadScripts(Set<Zpack> packs) {
        ZCLuaEngine.ZcLuaInstance luaEngine = ZCLuaEngine.getInstance();
        Map<ResourceLocation, Path> loadedScripts = new HashMap<>();

        packs.stream()
                .sorted(Comparator.comparing(pack ->
                        pack.outerPack().toAbsolutePath().normalize().toString()))
                .forEach(pack -> {
                    Path luaPath = pack.outerPack().resolve(AMMO_SCRIPT_PATH);

                    // scripts folder is optionally
                    if (!Files.isDirectory(luaPath)) {
                        return;
                    }

                    try (Stream<Path> stream = Files.walk(luaPath)) {
                        List<Path> scriptPaths = stream
                                .filter(Files::isRegularFile)
                                .filter(path -> path.getFileName().toString().endsWith(".lua"))
                                .sorted()
                                .toList();

                        for (Path scriptPath : scriptPaths) {
                            String relativePath = luaPath.relativize(scriptPath)
                                    .toString()
                                    .replace('\\', '/');
                            String resourcePath = relativePath.substring(
                                    0,
                                    relativePath.length() - ".lua".length()
                            );

                            final ResourceLocation scriptId;
                            try {
                                scriptId = new ResourceLocation(MOD_ID, resourcePath);
                            } catch (IllegalArgumentException exception) {
                                ZeroContactLogger.LOG.error(
                                        "Invalid Lua script path: {}",
                                        scriptPath,
                                        exception
                                );
                                continue;
                            }

                            Path previous = loadedScripts.putIfAbsent(scriptId, scriptPath);
                            if (previous != null) {
                                ZeroContactLogger.LOG.error(
                                        "Duplicated Lua script id {}: {} conflicts with {}",
                                        scriptId,
                                        scriptPath,
                                        previous
                                );
                                continue;
                            }

                            try (Reader reader = Files.newBufferedReader(scriptPath)) {
                                luaEngine.load(scriptId, reader);
                                ZeroContactLogger.LOG.info(
                                        "Loaded Lua script {} from {}",
                                        scriptId,
                                        scriptPath
                                );
                            } catch (Exception exception) {
                                ZeroContactLogger.LOG.error(
                                        "Failed to compile Lua script {} from {}",
                                        scriptId,
                                        scriptPath,
                                        exception
                                );
                            }
                        }
                    } catch (IOException e) {
                        ZeroContactLogger.LOG.error(
                                "Failed to scan Lua scripts in {}",
                                luaPath,
                                e
                        );
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
                ZeroContactLogger.LOG.error("Failed to load recipe data: ", e);
            } catch (JsonSyntaxException jsonSyntaxException) {
                ZeroContactLogger.LOG.error("Failed to parse json data: ", jsonSyntaxException);
            }
        }

        WorkBenchEntity.recipeData = merged.entrySet().stream()
                .map(e -> new GearRecipeData(e.getKey(), e.getValue()))
                .toList();
    }
}
