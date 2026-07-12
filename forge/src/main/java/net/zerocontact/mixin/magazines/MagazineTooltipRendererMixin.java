package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.client.tooltip.MagazineTooltipRenderer;
import com.raiiiden.taczmagazines.tooltip.MagazineTooltipData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.caliber.AmmoInjector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = MagazineTooltipRenderer.class, remap = false)
public class MagazineTooltipRendererMixin {
    @Mutable
    @Final
    @Shadow
    private final MagazineTooltipData data;

    public MagazineTooltipRendererMixin(MagazineTooltipData data) {
        this.data = data;
    }

    @ModifyArg(method = "renderImage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V"
            ))
    ItemStack replaceDisplayStack(ItemStack stack) {
        ItemStack mag = data.getMagazineStack();
        if (!mag.isEmpty()) {
            AmmoInjector.AmmoContext context = AmmoInjector.read(mag);
            if (!context.isEmpty()) {
                String id = context.caliber().variant();
                Item ammoItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
                if (ammoItem != null) {
                    return ammoItem.getDefaultInstance();
                }
            }
        }
        return stack;
    }
}
