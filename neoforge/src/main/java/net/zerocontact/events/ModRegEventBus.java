package net.zerocontact.events;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.RenderableItem;
import net.zerocontact.client.gui.AmmoSelectorScreen;
import net.zerocontact.armor.modular.client.screen.EquipmentScreen;
import net.zerocontact.item.backpack.client.screen.BackpackScreen;
import net.zerocontact.client.gui.WorkbenchScreen;
import net.zerocontact.client.interaction.KeyBindingHandler;
import net.zerocontact.client.particle.PersistentSignalSmokeParticle;
import net.zerocontact.client.tooltip.BallisticToolTipComponent;
import net.zerocontact.client.tooltip.ClientBallisticToolTipComponent;
import net.zerocontact.forge_registries.EntitiyRegistry;
import net.zerocontact.forge_registries.MenuRegistry;
import net.zerocontact.client.renderer.AccessoriesRender;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.forge_registries.ParticleRegistry;
import net.zerocontact.client.renderer.ArmedRaiderRender;
import net.zerocontact.item.block.Workbench;
import net.zerocontact.item.neoforge.AbstractGenerateGeoCurioItemImpl;
import net.zerocontact.item.helmet.GenerateHelmetGeoImpl;
import net.zerocontact.models.GenerateModel;
import net.zerocontact.registries.ItemsReg;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import static net.zerocontact.forge_registries.BlockRegistry.WORKBENCH_ENTITY;


public class ModRegEventBus {
    @EventBusSubscriber(modid = ZeroContact.MOD_ID)
    static class ServerDistribution {
        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.registerEntity(Capabilities.ItemHandler.ENTITY, EntitiyRegistry.ARMED_RAIDER.get(),
                    (raider, context) -> new InvWrapper(raider.getInventory()));
        }

        @SubscribeEvent
        public static void registerAttr(EntityAttributeCreationEvent event) {
            event.put(EntitiyRegistry.ARMED_RAIDER.get(), ArmedRaider.createAttributes().build());
        }
    }

    @EventBusSubscriber(modid = ZeroContact.MOD_ID, value = Dist.CLIENT)
    static class ClientDistribution {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(ModRegEventBus::RegCurioGeoItemRender);
        }

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(MenuRegistry.BACKPACK_CONTAINER.get(), BackpackScreen::new);
            event.register(MenuRegistry.WORKBENCH_MENU.get(), WorkbenchScreen::new);
            event.register(MenuRegistry.AMMO_SELECTOR.get(), AmmoSelectorScreen::new);
            event.register(MenuRegistry.EQUIPMENT_MENU.get(), EquipmentScreen::new);
        }

        @SubscribeEvent
        public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event){
            event.register(BallisticToolTipComponent.class, ClientBallisticToolTipComponent::new);
        }

        @SubscribeEvent
        public static void onRegisterMappings(RegisterKeyMappingsEvent event) {
            KeyBindingHandler.register(event);
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(
                    ParticleRegistry.PERSISTENT_SIGNAL_SMOKE.get(),
                    PersistentSignalSmokeParticle.Provider::new
            );
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EntitiyRegistry.ARMED_RAIDER.get(), ArmedRaiderRender::new);
            event.registerBlockEntityRenderer(
                    WORKBENCH_ENTITY.get(), arg -> new GeoBlockRenderer<>(
                            new GenerateModel<>(
                                    Workbench.texture,
                                    Workbench.model,
                                    Workbench.animation
                            )
                    )
            );
        }

        @SubscribeEvent
        public static void registerClientExtensions(RegisterClientExtensionsEvent event){
            ItemsReg.ITEMS.forEach(item->{
                Item item1 = item.get();
                if(item1 instanceof RenderableItem){
                    event.registerItem(
                            new IClientItemExtensions() {
                                @Override
                                public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                                    BlockEntityWithoutLevelRenderer renderer = GeoRenderProvider.of(item1).getGeoItemRenderer();
                                    return renderer != null ? renderer : IClientItemExtensions.super.getCustomRenderer();
                                }
                            },
                            item1
                    );
                }
            });
        }
    }

    private static void RegCurioGeoItemRender() {
        ItemsReg.ITEMS.forEach(itemRegistrySupplier -> {
            Item item = itemRegistrySupplier.get();
            if (item instanceof AbstractGenerateGeoCurioItemImpl abstractGenerateGeoCurioItem) {
                CuriosRendererRegistry.register(abstractGenerateGeoCurioItem, () -> new AccessoriesRender<>(abstractGenerateGeoCurioItem));
            }
            if (item instanceof GenerateHelmetGeoImpl generateHelmetGeo) {
                CuriosRendererRegistry.register(generateHelmetGeo, () -> new AccessoriesRender<>(generateHelmetGeo));
            }
        });
    }

}
