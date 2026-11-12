package net.zerocontact.caliber.capability;

import com.tacz.guns.api.item.IGun;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.zerocontact.ZeroContact;
import net.zerocontact.caliber.api.ICartridgeHolder;
import net.zerocontact.caliber.api.ICartridgeHolderAccess;
import net.zerocontact.item.backpack.BaseBackpack;

@EventBusSubscriber
public class CapabilityRegistry {
    public static final ItemCapability<ICartridgeHolder, Void> CARTRIDGE = ItemCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath(ZeroContact.MOD_ID, "cartridge"),
            ICartridgeHolder.class
    );

    @SubscribeEvent
    public static void registerCap(RegisterCapabilitiesEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IGun) {
                event.registerItem(
                        CARTRIDGE,
                        (stack, ctx) ->
                                ((ICartridgeHolderAccess) (Object) stack
                                ).zeroContact$getCartridgeHolder()
                        , item
                );
            }
            if (item instanceof BaseBackpack baseBackpack) {
                event.registerItem(
                        Capabilities.ItemHandler.ITEM,
                        (stack, ctx) -> new ComponentItemHandler(
                                stack,
                                DataComponents.CONTAINER,
                                baseBackpack.containerSize
                        ),
                        item
                );
            }
        }
    }
}
