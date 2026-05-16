package net.zerocontact.events;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.client.bundle.PlateBundle;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientPlateBundleRegEvent {
    @SubscribeEvent
    public static void bundlePlateTooltipsEvent(RenderTooltipEvent.GatherComponents tooltipEvent) {
        ItemStack tooltipStack = tooltipEvent.getItemStack();
        if (tooltipStack.getItem() instanceof IEquipmentTypeTag tag && tag.getArmorType().equals(IEquipmentTypeTag.EquipmentType.PLATE_CARRIER)) {
            NonNullList<ItemStack> plateStacks = NonNullList.of(ItemStack.EMPTY,
                    BaseArmorGeoImpl.getPlateStack(BaseArmorGeoImpl.FRONT_PLATE_SLOT, tooltipStack).copy(),
                    BaseArmorGeoImpl.getPlateStack(BaseArmorGeoImpl.BACK_PLATE_SLOT, tooltipStack).copy()
            );
            tooltipEvent.getTooltipElements().add(Either.right(new PlateBundle(plateStacks, 0, tooltipStack)));
        }
    }
}
