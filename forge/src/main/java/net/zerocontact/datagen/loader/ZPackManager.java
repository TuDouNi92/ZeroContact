package net.zerocontact.datagen.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.IAssetManager;
import net.zerocontact.api.IPackManager;
import net.zerocontact.datagen.ExperimentalBallisticData;
import net.zerocontact.datagen.GearRecipeData;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.RuntimeTypeAdapterFactory;
import net.zerocontact.events.CaliberVariantDamageHelper;
import net.zerocontact.item.block.WorkBenchEntity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ZPackManager implements IPackManager {
    private final RuntimeTypeAdapterFactory<ItemGenData> typeAdapterFactory =
            RuntimeTypeAdapterFactory
                    .of(ItemGenData.class, "type")
                    .registerSubtype(ItemGenData.Plate.class, "plate")
                    .registerSubtype(ItemGenData.Armor.class, "armor");
    private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeAdapterFactory).create();
    private static final Path defaultPath = Paths.get("config/zerocontact/packs");
    private final Set<Path> outerPacks = new HashSet<>();
    private static final Set<Pack> vanillaPacks = new HashSet<>();
    public static final ArrayList<ItemGenData> itemGenData = new ArrayList<>();
    private static final String DEFAULT_RECIPE_NAME = "default.json";
    private final ZAssetManager assetManager = new ZAssetManager();

    @Override
    public Gson getGson() {
        return gson;
    }

    @Override
    public Path getPacksPath() {
        return defaultPath;
    }

    @Override
    public Set<Path> getOuterPacks() {
        return outerPacks;
    }

    @Override
    public IAssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public void createDefaultPack() throws IOException {
        try (InputStream inputStream = ZeroContact.class.getResourceAsStream("/data/zerocontact/default_pack.zip")) {
            if (inputStream != null) {
                try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                    ZipEntry entry;
                    while ((entry = zipInputStream.getNextEntry()) != null) {
                        Path target = defaultPath.resolve("default_pack").resolve(entry.getName());
                        if (entry.isDirectory()) {
                            Files.createDirectories(target);
                        } else {
                            Files.createDirectories(target.getParent());
                            Files.copy(zipInputStream, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            } else {
                ZeroContactLogger.LOG.error("Default pack not found.");
            }
        }
    }

    private void findOuterPacks() throws IOException {
        try (Stream<Path> stream = Files.walk(defaultPath, 1)) {
            stream
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(defaultPath))
                    .forEach(packPath -> {
                        try {
                            outerPacks.add(packPath);
                        } catch (IllegalArgumentException e) {
                            ZeroContactLogger.LOG.error(e);
                        }
                    });
        }
    }

    @Override
    public <T extends IAssetManager> void loadOuterPack(T assetManager) {
        loadItems(assetManager);
        loadBallistics(assetManager);
        loadRecipes(assetManager);
    }

    @Override
    public void registerVanillaDataPackBundle() throws IllegalArgumentException {
        if (outerPacks.isEmpty()) return;
        outerPacks.forEach(path -> {
            String packId = path.getFileName().toString();
            Pack resourcePack = Pack.readMetaAndCreate(
                    packId,
                    Component.literal("ZeroContact ResPack"),
                    true,
                    (id) -> new PathPackResources(id, path, true),
                    PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
            Pack dataPack = Pack.readMetaAndCreate(
                    packId,
                    Component.literal("ZeroContact DataPack"),
                    true,
                    (id) -> new PathPackResources(id, path, true),
                    PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);
            vanillaPacks.add(resourcePack);
            vanillaPacks.add(dataPack);
        });
    }

    @Override
    public void init() {
        try {
            if (Files.notExists(defaultPath)) {
                Files.createDirectories(defaultPath);
            }
            createDefaultPack();
            findOuterPacks();
            loadOuterPack(assetManager);
            registerVanillaDataPackBundle();
        } catch (Exception e) {
            ZeroContactLogger.LOG.error(e);
        }
    }


    private <T extends IAssetManager> void loadItems(T assetManager) throws RuntimeException {
        outerPacks.forEach(pack -> {
            Path itemPath = pack.resolve("assets/" + MOD_ID + "/items");
            try {
                List<Path> itemList = assetManager.getJsonListPathsFromPath(itemPath);
                assetManager.deserializeFromJsonList(itemList, gson, ItemGenData.class, (data, __) -> itemGenData.add(data));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T extends IAssetManager> void loadBallistics(T assetManager) {
        outerPacks.forEach(pack -> {
            Path ballisticPath = pack.resolve("assets/" + MOD_ID + "/ammoDefinitions");
            try {
                List<Path> ammoList = assetManager.getJsonListPathsFromPath(ballisticPath);
                assetManager.deserializeFromJsonList(
                        ammoList,
                        gson,
                        ExperimentalBallisticData.class,
                        (data, __) ->
                                CaliberVariantDamageHelper.experimentalBallisticSet.
                                        add(
                                                new CaliberVariantDamageHelper.Caliber(
                                                        data.ammoId,
                                                        data.baseDamageFactor,
                                                        data.penetrationClass,
                                                        data.fleshDamage
                                                )
                                        )
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T extends IAssetManager> void loadRecipes(T assetManager) {
        outerPacks.forEach(pack -> {
            Path recipesPath = pack.resolve("assets/" + MOD_ID + "/gear_recipes");
            try {
                List<Path> recipePath = assetManager.getJsonListPathsFromPath(recipesPath);
                Map<String, List<GearRecipeData.IngredientItems>> overrideMap = new HashMap<>();
                assetManager.deserializeFromJsonList(recipePath, gson, GearRecipeData.class, (data, json) -> {
                    if (json.getFileName().toString().equals(DEFAULT_RECIPE_NAME)) {
                        WorkBenchEntity.recipeData.addAll(data.recipes);
                    } else {
                        data.recipes.forEach(recipe -> overrideMap.put(recipe.gearId, recipe.ingredientItems));
                        WorkBenchEntity.recipeData.forEach(recipe -> {
                            List<GearRecipeData.IngredientItems> override = overrideMap.get(recipe.gearId);
                            if (override != null) {
                                recipe.ingredientItems = override;
                            }
                            Set<String> existingId = WorkBenchEntity.recipeData.stream()
                                    .map(r -> r.gearId)
                                    .collect(Collectors.toSet());
                            overrideMap.forEach((key, value) -> {
                                if (!existingId.contains(key)) {
                                    WorkBenchEntity.recipeData.add(new GearRecipeData(key, value));
                                }
                            });
                        });
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    static class VanillaPackRegister {
        @SubscribeEvent
        public static void registerPacks(AddPackFindersEvent event) {
            if (vanillaPacks.isEmpty()) return;
            vanillaPacks.forEach(pack -> event.addRepositorySource(receiver -> receiver.accept(pack)));
        }
    }
}
