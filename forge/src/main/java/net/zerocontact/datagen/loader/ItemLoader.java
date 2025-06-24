package net.zerocontact.datagen.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.IItemLoader;
import net.zerocontact.datagen.ItemGenData;
import net.zerocontact.datagen.RuntimeTypeAdapterFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ItemLoader{
    public static ArrayList<ItemGenData> itemGenData = new ArrayList<>();
    private static final RuntimeTypeAdapterFactory<ItemGenData> typeFactory =
            RuntimeTypeAdapterFactory
                    .of(ItemGenData.class, "type")
                    .registerSubtype(ItemGenData.Plate.class, "plate")
                    .registerSubtype(ItemGenData.Armor.class, "armor");
    private static final Gson GSON = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();
    private static final Path LOAD_PATH = Paths.get("config/zerocontact/packs");
    private static final Set<Path> RPACK_PATHS = new HashSet<>();
    private static final Set<Pack> DATA_PACKS = new HashSet<>();

    public static void loadFromJson() {
        if (Files.notExists(LOAD_PATH)) {
            try {
                Files.createDirectories(LOAD_PATH);
            } catch (IOException e) {
                ZeroContactLogger.LOG.error(e);
                return;
            }
        }
        try {
            loadPack();
            loadResourceAndDataPack();
        } catch (IOException e) {
            ZeroContactLogger.LOG.error(e);
        }
    }

    private static void loadPack() throws IOException {
        try (Stream<Path> stream = Files.walk(LOAD_PATH, 1)) {
            stream
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(LOAD_PATH))
                    .forEach(packPath -> {
                        try {
                            RPACK_PATHS.add(packPath);
                        } catch (IllegalArgumentException e) {
                            ZeroContactLogger.LOG.error(e);
                            return;
                        }
                        Path resoucePackPath = packPath.resolve("assets").resolve(MOD_ID);
                        Path itemPath = resoucePackPath.resolve("items");
                        ZeroContactLogger.LOG.info("loading ItemPath");
                        IItemLoader.loadItemJson(itemPath,GSON);
                    });
        }
    }

    private static void loadResourceAndDataPack() {
        PackRepository resourcePackRepository = Minecraft.getInstance().getResourcePackRepository();
        if (RPACK_PATHS.isEmpty()) return;
        RPACK_PATHS.forEach((path) -> {
            String packId = path.getFileName().toString();
            Pack resourcePack = Pack.readMetaAndCreate(packId, Component.literal("ZeroContact ResPack"), true, (id) -> new PathPackResources(id, path, true), PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
            Pack dataPack = Pack.readMetaAndCreate(packId, Component.literal("ZeroContact DataPack"), true, (id) -> new PathPackResources(id, path, true), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);
            try {
                DATA_PACKS.add(dataPack);
            } catch (IllegalArgumentException e) {
                ZeroContactLogger.LOG.info("Error Adding pack {}", packId, e);
            }
            resourcePackRepository.addPackFinder((consumer) -> consumer.accept(resourcePack));
            ZeroContactLogger.LOG.info("Added pack {}", packId);
        });
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    static class DataPackLoader {
        @SubscribeEvent
        public static void loadDataPacks(AddPackFindersEvent event) {
            if (DATA_PACKS.isEmpty()) return;
            if (event.getPackType() != PackType.SERVER_DATA) return;
            DATA_PACKS.forEach(pack -> event.addRepositorySource(consumer -> consumer.accept(pack)));
        }
    }
}
