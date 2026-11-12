package net.zerocontact.forge_registries;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.zerocontact.item.block.WorkBenchEntity;
import net.zerocontact.item.block.Workbench;
import net.zerocontact.item.block.WorkbenchItem;

import java.util.LinkedHashSet;
import java.util.List;

import static net.zerocontact.ZeroContact.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class BlockRegistry {
    private static final LinkedHashSet<DeferredHolder<Item, ? extends Item>> BLOCK_ITEMS_REG_TAB = new LinkedHashSet<>();
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_DEFERRED_REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister<Block> BLOCK_DEFERRED_REGISTER = DeferredRegister.create(Registries.BLOCK, MOD_ID);
    private static final DeferredRegister<Item> ITEM_DEFERRED_REGISTER = DeferredRegister.create(Registries.ITEM, MOD_ID);
    public static final DeferredHolder<Block, Block> WORKBENCH_BLOCK = BLOCK_DEFERRED_REGISTER.register("thoughbook", Workbench::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WorkBenchEntity>> WORKBENCH_ENTITY =
            BLOCK_ENTITY_TYPE_DEFERRED_REGISTER.register("thoughbook", () -> BlockEntityType.Builder.of(
                    WorkBenchEntity::new, WORKBENCH_BLOCK.get()
            ).build(null));
    private static final DeferredHolder<Item, WorkbenchItem> WORKBENCH_ITEM = ITEM_DEFERRED_REGISTER.register("thoughbook", () -> new WorkbenchItem(WORKBENCH_BLOCK.get(), new Item.Properties()));


    @SubscribeEvent
    public static void attachToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ItemRegistry.DEFAULT_TAB_KEY)) {
            BLOCK_ITEMS_REG_TAB.forEach(item -> event.accept(item.get()));
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

