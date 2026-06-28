package net.zerocontact.datagen.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.IAssetManager;
import net.zerocontact.datagen.*;
import net.zerocontact.registries.ItemsReg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static net.zerocontact.forge_registries.ItemsRegForge.ITEMS_REG_TAB;

public class ZAssetManager implements IAssetManager {
    private final RuntimeTypeAdapterFactory<ItemGenData> typeAdapterFactory =
            RuntimeTypeAdapterFactory
                    .of(ItemGenData.class, "type")
                    .registerSubtype(ItemGenData.Plate.class, "plate")
                    .registerSubtype(ItemGenData.Armor.class, "armor")
                    .registerSubtype(ItemGenData.Loadout.class,"loadout");
    private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeAdapterFactory).create();

    @Override
    public Gson getGson() {
        return gson;
    }

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
    public <T> void deserializeFromManifest(Path json, Gson gson, Class<T> targetBeanClazz, Consumer<T> data) throws IOException, JsonSyntaxException {
        if (Files.notExists(json)) return;
        T rawData = gson.fromJson(Files.newBufferedReader(json), targetBeanClazz);
        data.accept(rawData);
    }

    @Override
    public void registerItems(LinkedHashMap<RegistrySupplier<? extends ItemLike>, String> regTabSet, DeferredRegister<Item> itemsDeferredRegister, WearableType... wearableTypes) {
        for (WearableType type : wearableTypes) {
            type.records().forEach(generationRecord -> {
                RegistrySupplier<? extends ItemLike> reg = itemsDeferredRegister.register(generationRecord.id(), generationRecord::item);
                regTabSet.put(reg, generationRecord.tabName());
                ZeroContactLogger.LOG.info("Reg {} for:{}", type.logDisplayName(), reg);
            });
        }
    }

    @Override
    public void register() {
        ZContentLoader.itemGenData.forEach((data, tab) -> ItemAdapter.ADAPTERS.forEach(adapter -> {
            if (data instanceof ItemGenData.Armor armor) {
                LinkedHashSet<GenerationRecord<?>> records = adapter.deserializeItems(armor, tab);
                if (records.isEmpty()) return;
                this.registerItems(ITEMS_REG_TAB, ItemsReg.ITEMS, new IAssetManager.WearableType(records, "ARMOR_LIKE"));
            } else if (data instanceof ItemGenData.Plate plate) {
                LinkedHashSet<GenerationRecord<?>> records = adapter.deserializeItems(plate, tab);
                if (records.isEmpty()) return;
                this.registerItems(ITEMS_REG_TAB, ItemsReg.ITEMS, new IAssetManager.WearableType(records, "PLATE_LIKE"));
            } else if(data instanceof ExperimentalBallisticData ammo){
                LinkedHashSet<GenerationRecord<?>> records = adapter.deserializeItems(ammo, tab);
                if(records.isEmpty())return;
                this.registerItems(ITEMS_REG_TAB,ItemsReg.ITEMS, new IAssetManager.WearableType(records,"AMMO"));
            }
            else if(data instanceof ItemGenData.Loadout loadout){
                LinkedHashSet<GenerationRecord<?>> records = adapter.deserializeItems(loadout, tab);
                if(records.isEmpty())return;
                this.registerItems(ITEMS_REG_TAB,ItemsReg.ITEMS, new IAssetManager.WearableType(records,"LOADOUT"));
            }
        }));
    }
}
