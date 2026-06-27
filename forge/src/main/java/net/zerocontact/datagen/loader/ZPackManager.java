package net.zerocontact.datagen.loader;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
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
import net.zerocontact.api.IPackManager;
import net.zerocontact.datagen.*;

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
    private static final Path defaultPath = Paths.get("config/zerocontact/packs");
    private final Set<Zpack> outerPacks = new HashSet<>();
    private static final Set<Pack> vanillaPacks = new HashSet<>();
    private final ZAssetManager assetManager;
    private final ZContentLoader contentLoader;
    private static final String DEFAULT_PACK_LOCATION = "/data/zerocontact/default_pack.zip";
    private static final String DEFAULT_PACK_NAME = "default_pack";
    private static final String MANIFEST_PATH = "manifest.json";
    private static final String CONFIG_PATH = "config/zerocontact/override.toml";
    private static final String CONFIG_NODE = "pack.default_pack_override";
    private boolean overridePack = true;

    public ZPackManager() {
        this.assetManager = new ZAssetManager();
        this.contentLoader = new ZContentLoader(assetManager);
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

    public void findOuterPacks() throws IOException {
        try (Stream<Path> stream = Files.walk(defaultPath, 1)) {
            stream
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(defaultPath))
                    .forEach(packPath -> {
                        try {
                            assetManager.deserializeFromManifest(
                                    packPath.resolve(MANIFEST_PATH), assetManager.getGson(),
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
    public void loadOuterPack() {
        contentLoader.load(outerPacks);
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
            loadOuterPack();
            registerVanillaDataPackBundle();
        } catch (Exception e) {
            ZeroContactLogger.LOG.error(e);
        }
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
