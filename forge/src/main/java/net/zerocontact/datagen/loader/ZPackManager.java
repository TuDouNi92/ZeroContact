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
import net.zerocontact.datagen.*;
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
    private final Set<Zpack> outerPacks = new HashSet<>();
    private static final Set<Pack> vanillaPacks = new HashSet<>();
    public static final LinkedHashMap<ItemGenData, String> itemGenData = new LinkedHashMap<>();
    private static final String DEFAULT_RECIPE_NAME = "default.json";
    private final ZAssetManager assetManager = new ZAssetManager();
    private static final String DEFAULT_PACK_LOCATION = "/data/zerocontact/default_pack.zip";
    private static final String DEFAULT_PACK_NAME = "default_pack";
    private static final String ITEM_PATH = "data/" + MOD_ID + "/items";
    private static final String AMMO_DEF_PATH = "data/" + MOD_ID + "/ammoDefinitions";
    private static final String RECIPES_PATH = "data/" + MOD_ID + "/gear_recipes";
    private static final String MANIFEST_PATH = "data/manifest.json";

    @Override
    public Gson getGson() {
        return gson;
    }

    @Override
    public Path getPacksPath() {
        return defaultPath;
    }

    @Override
    public Set<Zpack> getOuterPacks() {
        return outerPacks;
    }

    @Override
    public ZAssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public void createDefaultPack() throws IOException {
        try (InputStream inputStream = ZeroContact.class.getResourceAsStream(DEFAULT_PACK_LOCATION)) {
            if (inputStream != null) {
                try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                    ZipEntry entry;
                    while ((entry = zipInputStream.getNextEntry()) != null) {
                        Path target = defaultPath.resolve(DEFAULT_PACK_NAME).resolve(entry.getName());
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
                            assetManager.deserializeFromManifest(
                                    packPath.resolve(MANIFEST_PATH), gson,
                                    ManifestData.class,
                                    data -> outerPacks.add(
                                            new Zpack(
                                                    data.tabName(),
                                                    packPath
                                            )
                                    ));
                        } catch (IllegalArgumentException | IOException e) {
                            ZeroContactLogger.LOG.error(e);
                        }
                    });
        }
    }

    @Override
    public <T extends IAssetManager> void loadOuterPack(T assetManager) {
        loadItems();
        loadBallistics();
        loadRecipes();
    }

    @Override
    public void registerVanillaDataPackBundle() throws IllegalArgumentException {
        if (outerPacks.isEmpty()) return;
        outerPacks.forEach(path -> {
            String packId = path.outerPack().getFileName().toString();
            Pack resourcePack = Pack.readMetaAndCreate(
                    packId,
                    Component.literal("ZeroContact ResPack"),
                    true,
                    (id) -> new PathPackResources(id, path.outerPack(), true),
                    PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
            Pack dataPack = Pack.readMetaAndCreate(
                    packId,
                    Component.literal("ZeroContact DataPack"),
                    true,
                    (id) -> new PathPackResources(id, path.outerPack(), true),
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


    private void loadItems() throws RuntimeException {
        outerPacks.forEach(pack -> {
            Path itemPath = pack.outerPack().resolve(ITEM_PATH);
            try {
                List<Path> itemList = assetManager.getJsonListPathsFromPath(itemPath);
                assetManager.deserializeFromJsonList(itemList, gson, ItemGenData.class, (data, __) -> itemGenData.put(data, pack.tab()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadBallistics() {
        outerPacks.forEach(pack -> {
            Path ballisticPath = pack.outerPack().resolve(AMMO_DEF_PATH);
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

    private void loadRecipes() {
        outerPacks.forEach(pack -> {
            Path recipesPath = pack.outerPack().resolve(RECIPES_PATH);
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
