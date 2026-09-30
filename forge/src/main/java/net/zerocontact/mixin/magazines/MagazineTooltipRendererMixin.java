package net.zerocontact.mixin.magazines;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.raiiiden.taczmagazines.client.tooltip.MagazineTooltipRenderer;
import com.raiiiden.taczmagazines.tooltip.MagazineTooltipData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.client.tooltip.MagazineAmmoTooltip;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MagazineTooltipRenderer.class)
public abstract class MagazineTooltipRendererMixin {
    @Final
    @Shadow
    private MagazineTooltipData data;

    @Shadow
    public abstract int getWidth(Font font);

    @ModifyArg(method = "renderImage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V"
            ), index = 0)
    private ItemStack replaceDisplayStack(ItemStack stack) {
        ItemStack top = MagazineAmmoTooltip.topAmmo(data.getMagazineStack());
        return top.isEmpty() ? stack : top;
    }

    @Inject(method = "getWidth", at = @At("RETURN"), cancellable = true)
    private void addAmmoPreviewWidth(Font font, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() + MagazineAmmoTooltip.extraWidth(font, data.getMagazineStack()));
    }

    @ModifyExpressionValue(method = "renderImage", at = @At(value = "INVOKE",
            target = "Lcom/raiiiden/taczmagazines/client/tooltip/MagazineTooltipRenderer;getWidth(Lnet/minecraft/client/gui/Font;)I"))
    private int keepCapacityBarWidth(int width, Font font, int x, int y, GuiGraphics graphics) {
        return width - MagazineAmmoTooltip.extraWidth(font, data.getMagazineStack());
    }

    @Inject(method = "renderImage", at = @At("TAIL"))
    private void renderAmmoPreview(Font font, int x, int y, GuiGraphics graphics, CallbackInfo ci) {
        ItemStack magazine = data.getMagazineStack();
        int baseWidth = getWidth(font) - MagazineAmmoTooltip.extraWidth(font, magazine);
        MagazineAmmoTooltip.render(font, graphics, magazine, x + baseWidth, y);
    }
}
