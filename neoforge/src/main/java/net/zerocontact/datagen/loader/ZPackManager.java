package net.zerocontact.datagen.loader;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zerocontact.ZeroContact;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.datagen.IPackManager;
import net.zerocontact.datagen.model.ManifestPOJO;
import net.zerocontact.datagen.model.ZPack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZPackManager implements IPackManager {
    private static final Path LOCAL_PACK_ROOT = Paths.get("config/zerocontact/packs");
    public static final String OLD_DEFAULT_PACK = "zero_contact";
    public final Set<ZPack> outerPacks = new HashSet<>();
    private static final Set<VPack> vanillaPacks = new HashSet<>();
    private final ZAssetManager assetManager;
    private final ZContentLoader contentLoader;
    private static final String DEFAULT_PACK_LOCATION = "/data/zerocontact/default_pack.zip";
    private static final String MANIFEST_PATH = "manifest.json";
    private static final String CONFIG_PATH = "config/zerocontact/override.toml";
    private static final String CONFIG_NODE = "pack.default_pack_override";
    private boolean overridePack = true;
    private static ZPackManager singleton;

    private record VPack(PackType type, Pack pack) {
        public boolean isDataPack() {
            return type == PackType.SERVER_DATA;
        }
    }

    private ZPackManager() {
        this.assetManager = new ZAssetManager();
        this.contentLoader = new ZContentLoader(assetManager);
    }

    public static ZPackManager getInstance() {
        if (singleton == null) {
            singleton = new ZPackManager();
            return singleton;
        }
        return singleton;
    }

    @Override
    public ZAssetManager getAssetManager() {
        return assetManager;
    }

    private void loadConfig() {
        CommentedFileConfig config = CommentedFileConfig.builder(Path.of(CONFIG_PATH)).onFileNotFound(FileNotFoundAction.CREATE_EMPTY).autosave().build();
        config.load();
        if (config.isEmpty()) {
            config.set(CONFIG_NODE, overridePack);
        }
        this.overridePack = config.getOrElse(CONFIG_NODE, overridePack);
        config.close();
    }

    @Override
    public void createDefaultPack() throws IOException {
        loadConfig();
        if (!overridePack) {
            ZeroContactLogger.LOG.warn("The Default Pack override flag is set to False!");
            return;
        }
        try (InputStream inputStream = ZeroContact.class.getResourceAsStream(DEFAULT_PACK_LOCATION)) {
            if (inputStream != null) {
                try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                    ZipEntry entry;
                    while ((entry = zipInputStream.getNextEntry()) != null) {

                        String name = entry.getName();
                        Path localPack = LOCAL_PACK_ROOT.resolve(name);

                        if (entry.isDirectory()) {
                            Files.createDirectories(localPack);
                        } else {
                            Files.createDirectories(localPack.getParent());
                            Files.copy(zipInputStream, localPack, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            } else {
                ZeroContactLogger.LOG.error("Default pack not found.");
            }
        }
    }

    public void findOuterPacks() throws IOException {
        try (Stream<Path> stream = Files.walk(LOCAL_PACK_ROOT, 1)) {
            stream
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(LOCAL_PACK_ROOT))
                    .forEach(packPath -> {
                        try {
                            assetManager.deserializeFromManifest(
                                    packPath.resolve(MANIFEST_PATH), assetManager.getGson(),
                                    ManifestPOJO.class,
                                    data -> {
                                        String packName = data.tabName();
                                        if (packName.equals(OLD_DEFAULT_PACK)) {
                                            ZeroContactLogger.LOG.warn("Detected deprecated pack: {}. Ignoring", packName);
                                        } else {
                                            outerPacks.add(
                                                    new ZPack(
                                                            packName,
                                                            packPath,
                                                            data.author(),
                                                            data.version())
                                            );
                                        }
                                    });
                        } catch (Exception e) {
                            ZeroContactLogger.LOG.error(e);
                        }
                    });
        }
    }

    @Override
    public void loadOuterPack() {
        contentLoader.load(outerPacks);
    }

    @Override
    public void registerVanillaDataPackBundle() throws IllegalArgumentException {
        if (outerPacks.isEmpty()) return;
        outerPacks.forEach(path -> {
            String packId = path.outerPack().getFileName().toString();
            Pack resourcePack = Pack.readMetaAndCreate(
                    new PackLocationInfo(
                            packId,
                            Component.literal("ZeroContact ResPack"),
                            PackSource.BUILT_IN,
                            Optional.empty()
                    ),
                    new PathPackResources.PathResourcesSupplier(path.outerPack()),
                    PackType.CLIENT_RESOURCES,
                    new PackSelectionConfig(
                            true,
                            Pack.Position.TOP,
                            false
                    )
            );

            Pack dataPack = Pack.readMetaAndCreate(
                    new PackLocationInfo(
                            packId,
                            Component.literal("ZeroContact DataPack"),
                            PackSource.BUILT_IN,
                            Optional.empty()
                    ),
                    new PathPackResources.PathResourcesSupplier(path.outerPack()),
                    PackType.SERVER_DATA,
                    new PackSelectionConfig(
                            true,
                            Pack.Position.TOP,
                            false
                    )
            );


            vanillaPacks.add(new VPack(PackType.CLIENT_RESOURCES, resourcePack));
            vanillaPacks.add(new VPack(PackType.SERVER_DATA, dataPack));
        });
    }

    @Override
    public void init() {
        try {
            if (Files.notExists(LOCAL_PACK_ROOT)) {
                Files.createDirectories(LOCAL_PACK_ROOT);
            }
            createDefaultPack();
            findOuterPacks();
            loadOuterPack();
            registerVanillaDataPackBundle();
        } catch (Exception e) {
            ZeroContactLogger.LOG.error(e);
        }
    }

    @EventBusSubscriber(modid = ZeroContact.MOD_ID)
    static class VanillaPackRegister {
        @SubscribeEvent
        public static void registerPacks(AddPackFindersEvent event) {
            if (vanillaPacks.isEmpty()) return;
            vanillaPacks.forEach(vPack ->
                    event.addRepositorySource(receiver ->
                            {
                                switch (event.getPackType()) {
                                    case SERVER_DATA -> {
                                        if (vPack.isDataPack()) receiver.accept(vPack.pack);
                                    }
                                    case CLIENT_RESOURCES -> {
                                        if (!vPack.isDataPack()) receiver.accept(vPack.pack);
                                    }
                                }
                            }
                    ));
        }
    }
}
