package net.zerocontact.api;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.ZeroContact;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.datagen.ExperimentalBallisticData;
import net.zerocontact.datagen.GearRecipeData;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.events.CaliberVariantDamageHelper;
import net.zerocontact.item.block.WorkBenchEntity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.zerocontact.datagen.loader.AssetManager.itemGenData;

public interface AssetHelper {
    interface IFiles extends AssetHelper {
        static List<Path> getJsonList(Path path) throws IOException {
            try (Stream<Path> stream = Files.walk(path)) {
                return stream.filter(name -> name.toString().endsWith(".json"))
                        .toList();
            }
        }

        static void loadItemJson(Path itemPath, Gson GSON) {
            if (!Files.exists(itemPath)) return;
            ZeroContactLogger.LOG.info("Try path: {} ", itemPath);
            try {
                getJsonList(itemPath).forEach(itemJsonPath -> {
                    ZeroContactLogger.LOG.info("Try json: {} ", itemJsonPath);
                    try {
                        Type listType = new TypeToken<ItemGenData>() {
                        }.getType();
                        ItemGenData item0 = GSON.fromJson(Files.newBufferedReader(itemJsonPath), listType);
                        itemGenData.add(item0);
                        if (item0 instanceof ItemGenData.Armor item) {
                            ZeroContactLogger.LOG.info("Added item: {} \n {}", item.id, GSON.toJson(item));
                        } else if (item0 instanceof ItemGenData.Plate item) {
                            ZeroContactLogger.LOG.info("Added item: {} \n {}", item.id, GSON.toJson(item));
                        }
                    } catch (IOException e) {
                        ZeroContactLogger.LOG.error(e);
                    }
                });
            } catch (IOException e) {
                ZeroContactLogger.LOG.error(e);
            }
        }

        static void loadBallisticJson(Path path, Gson GSON) {
            if (!Files.exists(path)) return;
            try {
                getJsonList(path).forEach(jsonPath -> {
                    ZeroContactLogger.LOG.info("Try:{}", jsonPath);
                    //Deserialize data from json and collect.
                    try {
                        ExperimentalBallisticData data = GSON.fromJson(Files.newBufferedReader(jsonPath), ExperimentalBallisticData.class);
                        if (!CaliberVariantDamageHelper.experimentalBallisticSet.add(
                                new CaliberVariantDamageHelper.Caliber(
                                        data.ammoId,
                                        data.baseDamageFactor,
                                        data.penetrationClass,
                                        data.fleshDamage
                                )
                        )) {
                            ZeroContactLogger.LOG.error("Failed to assert {}: Duplicated ammo data!!", data.ammoId);
                        }
                    } catch (IOException e) {
                        ZeroContactLogger.LOG.error(e);
                    }
                });
            } catch (IOException e) {
                ZeroContactLogger.LOG.error(e);
            }
        }

        static void copyRecipes(Path recipe) {
            if (!Files.exists(recipe.resolve("default.json"))) {
                ZeroContactLogger.LOG.info("No default recipe config found, copying new one to {}", recipe);
                try (InputStream inputStream = ZeroContact.class.getResourceAsStream("/data/zerocontact/recipes/default.json")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, recipe.resolve("default.json"));
                    } else {
                        ZeroContactLogger.LOG.error("Recipe config not found inside datapack!");
                    }
                } catch (Exception e) {
                    ZeroContactLogger.LOG.error(e);
                }
            }
        }

        static void loadRecipes(Path recipe, Gson GSON) {
            List<IOException> exceptions = new ArrayList<>();
            if (!Files.exists(recipe)) return;
            try {
                Map<String, List<GearRecipeData.IngredientItems>> overrideMap = new HashMap<>();
                getJsonList(recipe).forEach(json -> {
                    try {
                        GearRecipeData deserializedData = GSON.fromJson(Files.newBufferedReader(json), GearRecipeData.class);
                        if (json.getFileName().toString().equals("default.json")) {
                            WorkBenchEntity.recipeData.addAll(deserializedData.recipes);
                        } else {
                            for(GearRecipeData recipeData:deserializedData.recipes){
                                overrideMap.put(recipeData.gearId, recipeData.ingredientItems);
                            }
                            for (GearRecipeData baseData : WorkBenchEntity.recipeData) {
                                List<GearRecipeData.IngredientItems> override = overrideMap.get(baseData.gearId);
                                if (override != null) {
                                    baseData.ingredientItems = override;
                                }
                            }
                            Set<String> existingId = WorkBenchEntity.recipeData.stream()
                                    .map(r -> r.gearId)
                                    .collect(Collectors.toSet());
                            for (var e : overrideMap.entrySet()) {
                                if (!existingId.contains(e.getKey())) {
                                    WorkBenchEntity.recipeData.add(new GearRecipeData(e.getKey(), e.getValue()));
                                }
                            }
                        }
                    } catch (IOException e) {
                        exceptions.add(e);
                    }
                });
            } catch (IOException e) {
                exceptions.add(e);
                ZeroContactLogger.LOG.error(exceptions);
            }
        }
    }

    interface GeneratableItem extends AssetHelper {
        void deserializeItems();
    }

    record WearableType(Set<GenerationRecord<?>> records, String logDisplayName) {
    }

    static void registerGeneratedItems(LinkedHashSet<RegistrySupplier<? extends ItemLike>> regTabSet, DeferredRegister<Item> itemsDeferredRegister, WearableType... wearableTypes) {
        itemsDeferredRegister.forEach(itemRegistrySupplier -> {
            if (itemRegistrySupplier.get() instanceof GeneratableItem generatableItem) {
                generatableItem.deserializeItems();
            }
        });
        for (WearableType type : wearableTypes) {
            type.records.forEach(generationRecord -> {
                RegistrySupplier<? extends ItemLike> reg = itemsDeferredRegister.register(generationRecord.id(), generationRecord::item);
                regTabSet.add(reg);
                ZeroContactLogger.LOG.info("Reg {} for:{}", type.logDisplayName, reg);
            });
        }
    }
}
