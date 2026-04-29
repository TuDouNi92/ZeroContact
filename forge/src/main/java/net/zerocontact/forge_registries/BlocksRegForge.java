package net.zerocontact.forge_registries;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import net.zerocontact.item.block.GeoBlockItem;
import net.zerocontact.item.block.WorkBenchEntity;
import net.zerocontact.item.block.Workbench;
import net.zerocontact.item.block.WorkbenchItem;
import net.zerocontact.registries.ItemsReg;

import java.util.LinkedHashSet;
import java.util.List;

import static net.zerocontact.ZeroContact.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlocksRegForge {
    private static final LinkedHashSet<RegistryObject<? extends ItemLike>> BLOCK_ITEMS_REG_TAB = new LinkedHashSet<>();
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    private static final DeferredRegister<Block> BLOCK_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEM_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final RegistryObject<Block> WORKBENCH_BLOCK = BLOCK_DEFERRED_REGISTER.register("thoughbook", Workbench::new);
    public static final RegistryObject<BlockEntityType<WorkBenchEntity>> WORKBENCH_ENTITY =
            BLOCK_ENTITY_TYPE_DEFERRED_REGISTER.register("thoughbook", () -> BlockEntityType.Builder.of(
                    WorkBenchEntity::new, WORKBENCH_BLOCK.get()
            ).build(null));
    private static final RegistryObject<WorkbenchItem> WORKBENCH_ITEM = ITEM_DEFERRED_REGISTER.register("thoughbook", () -> new WorkbenchItem(WORKBENCH_BLOCK.get(), new Item.Properties()));


    @SubscribeEvent
    public static void attachToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab().equals(ItemsReg.ZERO_CONTACT.get())) {
            BLOCK_ITEMS_REG_TAB.forEach(event::accept);
        }
    }

    @SubscribeEvent
    public static void onReg(RegisterEvent event) {
        BLOCK_ITEMS_REG_TAB.addAll(
                List.of(
                        WORKBENCH_ITEM
                )
        );
    }

    public static void register(IEventBus eventBus) {
        BLOCK_DEFERRED_REGISTER.register(eventBus);
        BLOCK_ENTITY_TYPE_DEFERRED_REGISTER.register(eventBus);
        ITEM_DEFERRED_REGISTER.register(eventBus);
    }
}

