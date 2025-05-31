package net.zerocontact.datagen.loader;

import com.google.gson.Gson;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.PackGenData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class ItemLoader {
    public static ArrayList<ItemGenData> itemGenData = new ArrayList<>();
    private static final Gson GSON = new Gson();

    public static void loadFromJson() {
        Path LOAD_PATH = Paths.get("config/zerocontact/packs");
        if (Files.notExists(LOAD_PATH)) {
            try {
                Files.createDirectories(LOAD_PATH);
            } catch (IOException e) {
                ZeroContactLogger.LOG.error(e);
            }
        }
        try {
            loadPackJson(LOAD_PATH);
        } catch (IOException e) {
            ZeroContactLogger.LOG.error(e);
        }
    }

    private static void loadPackJson(Path LOAD_PATH) throws IOException {
        try (Stream<Path> stream = Files.walk(LOAD_PATH)) {
            stream.filter(Files::isDirectory).forEach(packPath -> {
                Path packJson = packPath.resolve("pack.json");
                if (Files.exists(packJson)) {
                    try {
                        PackGenData data = GSON.fromJson(Files.newBufferedReader(packJson), PackGenData.class);
                        if (data.name == null) return;
                        Path itemPath = packPath.resolve("assets").resolve(data.name).resolve("items");
                        ZeroContactLogger.LOG.info("Load pack from {}", itemPath);
                        loadItemJson(itemPath);
                    } catch (IOException e) {
                        ZeroContactLogger.LOG.error(e);
                    }
                }
            });
        }
    }

    private static void loadItemJson(Path itemPath) throws IOException {
        try (Stream<Path> stream = Files.walk(itemPath)) {
            ZeroContactLogger.LOG.info("Try path: {} ", itemPath);
            stream.filter(name -> name.toString().endsWith(".json")).toList().forEach(itemJsonPath -> {
                ZeroContactLogger.LOG.info("Try json: {} ", itemJsonPath);
                if (Files.exists(itemJsonPath)) {
                    try {
                        ItemGenData item = GSON.fromJson(Files.newBufferedReader(itemJsonPath), ItemGenData.class);
                        itemGenData.add(item);
                        ZeroContactLogger.LOG.info("Added item: {}", item.id);
                    } catch (IOException e) {
                        ZeroContactLogger.LOG.error(e);
                    }
                }
            });
        }
    }
}
