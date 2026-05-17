package net.zerocontact.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.zerocontact.datagen.GenerationRecord;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ApiStatus.Internal
public interface IAssetManager {
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

    /**
     * This should Register Items that should implemented {@link GeneratableItem}
     */
    void registerItems(LinkedHashSet<RegistrySupplier<? extends ItemLike>> regTabSet, DeferredRegister<Item> itemsDeferredRegister, WearableType... wearableTypes);

    record WearableType(Set<GenerationRecord<?>> records, String logDisplayName) {
    }

    interface GeneratableItem {
        void deserializeItems();
    }
}
