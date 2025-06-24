package net.zerocontact.api;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.loader.ItemLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import static net.zerocontact.datagen.loader.ItemLoader.itemGenData;

public interface IItemLoader {
        static void loadItemJson(Path itemPath, Gson GSON) {
            if (!Files.exists(itemPath)) return;
            try (Stream<Path> stream = Files.walk(itemPath)) {
                ZeroContactLogger.LOG.info("Try path: {} ", itemPath);
                stream.filter(name -> name.toString().endsWith(".json")).forEach(itemJsonPath -> {
                    ZeroContactLogger.LOG.info("Try json: {} ", itemJsonPath);
                    if (Files.exists(itemJsonPath)) {
                        try {
                            Type listType = new TypeToken<ItemGenData>(){}.getType();
                            ItemGenData item0 = GSON.fromJson(Files.newBufferedReader(itemJsonPath), listType);
                            itemGenData.add(item0);
                            if(item0 instanceof ItemGenData.Armor item){
                                ZeroContactLogger.LOG.info("Added item: {} \n {}", item.id,GSON.toJson(item));
                            } else if (item0 instanceof ItemGenData.Plate item) {
                                ZeroContactLogger.LOG.info("Added item: {} \n {}", item.id,GSON.toJson(item));
                            }
                        } catch (IOException e) {
                            ZeroContactLogger.LOG.error(e);
                        }
                    }
                });
            } catch (IOException e) {
                ZeroContactLogger.LOG.error(e);
            }
        }
}
