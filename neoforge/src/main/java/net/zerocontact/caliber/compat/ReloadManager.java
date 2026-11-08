package net.zerocontact.caliber.compat;

import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.zerocontact.curios.CuriosConstants;
import net.zerocontact.events.EventUtil;

public class ReloadManager {
    public enum ReloadSource {
        VANILLA,
        RIGS
    }

    public record ReloadInventory(
            ReloadSource source,
            IItemHandler rawHandler,
            ItemStack containerStack,
            Runnable save
    ) {
    }

    public static ReloadInventory resolveReloadInv(LivingEntity shooter) {
        ReloadSource source;
        ItemStack containerStack = ItemStack.EMPTY;
        IItemHandler handler;
        ItemStack rigs = EventUtil.getCuriosStackFirst(shooter, CuriosConstants.RIGS);
        Runnable saveFunc = ()->{};
        if (!rigs.isEmpty()) {
            source = ReloadSource.RIGS;
            containerStack = rigs;
            handler = containerStack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(new ItemStackHandler());
            saveFunc = ()->{
              if(handler instanceof ItemStackHandler itemStackHandler){
                  rigs.getOrCreateTag().put(
                          "inventory",
                          itemStackHandler.serializeNBT().getList("Items", Tag.TAG_COMPOUND)
                  );
              }
            };
        } else {
            source = ReloadSource.VANILLA;
            handler = shooter.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(new ItemStackHandler());
        }
        return new ReloadInventory(
                source,
                handler,
                containerStack,
                saveFunc
        );

    }
}
