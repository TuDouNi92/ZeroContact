package net.zerocontact.api;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.datagen.ExperimentalBallisticData;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.events.CaliberVariantDamageHelper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static net.zerocontact.datagen.loader.ItemLoader.itemGenData;

public interface IItemLoader {
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
                        ZeroContactLogger.LOG.error("Failed to assert {}: Duplicated ammo data!!",data.ammoId);
                    }
                } catch (IOException e) {
                    ZeroContactLogger.LOG.error(e);
                }
            });
        } catch (IOException e) {
            ZeroContactLogger.LOG.error(e);
        }
    }
}
