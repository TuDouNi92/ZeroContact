package net.zerocontact.item.backpack;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.NetworkHooks;
import net.zerocontact.api.ArmorTypeTag;
import net.zerocontact.api.Toggleable;
import net.zerocontact.client.menu.MySimpleContainerMenu;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import net.zerocontact.item.forge.AbstractGenerateGeoCurioItemImpl;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import java.util.function.Consumer;

public class BaseBackpack extends AbstractGenerateGeoCurioItemImpl implements ArmorTypeTag, Toggleable.Backpack {
    private final int containerSize;
    private boolean isOpen = false;

    public BaseBackpack(String id, int defaultDurability, ResourceLocation texture, ResourceLocation model, ResourceLocation animation, int containerSize) {
        super(id, defaultDurability, texture, model, animation);
        this.containerSize = containerSize;
    }

    @Override
    public @NotNull ArmorType getArmorType() {
        return ArmorType.BACKPACK;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        if (isOpen && !slotContext.entity().level().isClientSide() && slotContext.entity() instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((id, inv, __) -> new MySimpleContainerMenu(id, inv, containerSize), Component.literal(this.id)), buf -> buf.writeInt(containerSize));
        }
    }

    @Override
    public void setToggling(boolean open) {
        this.isOpen = open;
    }

    @Override
    public boolean getToggling() {
        return this.isOpen;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ArmorRender.ItemRender<BaseArmorGeoImpl> itemRender;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (itemRender == null) {
                    this.itemRender = new ArmorRender.ItemRender<>(new GenerateModel<>(texture, model, animation));
                }
                return itemRender;
            }
        });
    }
}
