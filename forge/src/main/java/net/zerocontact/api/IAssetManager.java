package net.zerocontact.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.datagen.GenerationRecord;
import net.zerocontact.datagen.ItemGenData;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ApiStatus.Internal
public interface IAssetManager {

    Gson getGson();

    //Collect JSON paths from Path
    List<Path> getJsonListPathsFromPath(Path path) throws IOException;

    /**
     * Deserialize Items that are from JSON. Providing a bean object result for a custom operation
     *
     * @param <T>             The GSON bean
     * @param list            The list of JSON paths
     * @param targetBeanClazz the GSON bean class
     * @param data            Returned bean object
     */
    <T> void deserializeFromJsonList(List<Path> list, Gson gson, Class<T> targetBeanClazz, BiConsumer<T, Path> data) throws IOException, JsonSyntaxException;

    <T> void deserializeFromManifest(Path json, Gson gson, Class<T> targetBeanClazz, Consumer<T> data) throws IOException, JsonSyntaxException;

    /**
     * This should Register Items that should implemented {@link GeneratableItem}
     */
    void registerItems(LinkedHashMap<RegistrySupplier<? extends ItemLike>, String> regTabSet, DeferredRegister<Item> itemsDeferredRegister, WearableType... wearableTypes);

    void register();

    record WearableType(LinkedHashSet<GenerationRecord<?>> records, String logDisplayName) {
    }

    interface GeneratableItem {
        default <T extends ItemGenData.Armor> LinkedHashSet<GenerationRecord<?>> deserializeItems(T data, String tab) {
            return new LinkedHashSet<>();
        }

        default <T extends ItemGenData.Plate> LinkedHashSet<GenerationRecord<?>> deserializeItems(T data, String tab) {
            return new LinkedHashSet<>();
        }

    }
}
