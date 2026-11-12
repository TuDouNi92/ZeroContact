package net.zerocontact.caliber.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zerocontact.curios.CuriosConstants;
import net.zerocontact.events.EventUtil;
import net.zerocontact.item.rigs.BaseRigs;

import java.util.Optional;

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
            handler = Optional.ofNullable(containerStack.getCapability(Capabilities.ItemHandler.ITEM)).orElse(new ItemStackHandler());
            saveFunc = ()->{
              if(handler instanceof ItemStackHandler itemStackHandler){
                  if(rigs.getItem() instanceof BaseRigs baseRigs){
                      baseRigs.manualSave(rigs,itemStackHandler,shooter.level().registryAccess());
                  }
              }
            };
        } else {
            source = ReloadSource.VANILLA;
            handler = Optional.ofNullable(shooter.getCapability(Capabilities.ItemHandler.ENTITY)).orElse(new ItemStackHandler());
        }
        return new ReloadInventory(
                source,
                handler,
                containerStack,
                saveFunc
        );

    }
}
