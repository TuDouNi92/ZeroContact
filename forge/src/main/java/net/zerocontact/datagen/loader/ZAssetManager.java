package net.zerocontact.datagen.loader;

import com.google.gson.Gson;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.IAssetManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class ZAssetManager implements IAssetManager {
    @Override
    public List<Path> getJsonListPathsFromPath(Path path) throws IOException {
        try (Stream<Path> stream = Files.walk(path)) {
            return stream.filter(name -> name.toString().endsWith(".json"))
                    .toList();
        }
    }

    @Override
    public <T> void deserializeFromJsonList(List<Path> list, Gson gson, Class<T> targetBeanClazz, BiConsumer<T, Path> data) throws RuntimeException {
        list.forEach(itemJsonPath -> {
            ZeroContactLogger.LOG.info("Try loading JSON at；{}", itemJsonPath);
            try {
                T rawData = gson.fromJson(Files.newBufferedReader(itemJsonPath), targetBeanClazz);
                data.accept(rawData, itemJsonPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void registerItems(LinkedHashSet<RegistrySupplier<? extends ItemLike>> regTabSet, DeferredRegister<Item> itemsDeferredRegister, WearableType... wearableTypes) {
        itemsDeferredRegister.forEach(itemRegistrySupplier -> {
            if (itemRegistrySupplier.get() instanceof GeneratableItem generatableItem) {
                generatableItem.deserializeItems();
            }
        });
        for (WearableType type : wearableTypes) {
            type.records().forEach(generationRecord -> {
                RegistrySupplier<? extends ItemLike> reg = itemsDeferredRegister.register(generationRecord.id(), generationRecord::item);
                regTabSet.add(reg);
                ZeroContactLogger.LOG.info("Reg {} for:{}", type.logDisplayName(), reg);
            });
        }
    }
}
