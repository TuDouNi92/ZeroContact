package net.zerocontact.mixin.tacz;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.item.AmmoBoxItem;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AmmoBoxItem.class, remap = false)
public class AmmoBoxItemMixin {

    @Unique
    private boolean zeroContact$updateCartridge(ItemStack ammoBoxStack, Slot slot) {
        AmmoInjector.AmmoContext contextFromAmmo = AmmoInjector.get().readCxtFromStack(slot.getItem());
        if (!(ammoBoxStack.getItem() instanceof IAmmoBox box)) return false;
        if (box.getAmmoCount(ammoBoxStack) <= 0) {
            AmmoInjector.get().writeCxtToStack(contextFromAmmo, ammoBoxStack);
        } else if (box.isCreative(ammoBoxStack) && box.getAmmoId(ammoBoxStack).equals(DefaultAssets.EMPTY_AMMO_ID)) {
            AmmoInjector.get().writeCxtToStack(contextFromAmmo, ammoBoxStack);
            box.setAmmoId(ammoBoxStack, ResourceLocation.parse(contextFromAmmo.caliber().id()));
        }
        AmmoInjector.AmmoContext contextFromMag = AmmoInjector.get().readCxtFromStack(ammoBoxStack);
        return !contextFromAmmo.caliber().equals(contextFromMag.caliber());
    }

    @Inject(method = "lambda$overrideStackedOnOther$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tacz/guns/item/AmmoBoxItem;setAmmoId(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/resources/ResourceLocation;)V"),
            cancellable = true)
    public void updateExtractBox(int boxAmmoCount, ResourceLocation boxAmmoId, Slot slot, ItemStack ammoBox, Player player, CommonAmmoIndex index, CallbackInfoReturnable<Boolean> cir) {
        boolean shouldCancel = zeroContact$updateCartridge(ammoBox, slot);
        if (shouldCancel) cir.cancel();
    }

    @Inject(method = "overrideStackedOnOther",
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/ResourceLocation;equals(Ljava/lang/Object;)Z",
                    ordinal = 2,
                    remap = false
            ), cancellable = true)
    public void updateInsertBox(ItemStack ammoBox, Slot slot, ClickAction action, Player player, CallbackInfoReturnable<Boolean> cir) {
        boolean shouldCancel = zeroContact$updateCartridge(ammoBox, slot);
        if (shouldCancel) cir.cancel();
    }


    @ModifyVariable(method = "lambda$overrideStackedOnOther$0", at = @At("STORE"), name = "takeAmmo")
    private ItemStack replaceBullet(ItemStack takeAmmo, int boxAmmoCount, ResourceLocation boxAmmoId,
                                    Slot slot, ItemStack ammoBox, Player player, CommonAmmoIndex index) {
        AmmoInjector.AmmoContext context = AmmoInjector.get().readCxtFromStack(ammoBox);
        if (context != null && !context.isEmpty()) {
            Item ammoItem = AmmoInjector.get().getItemForVariant(context);
            if (ammoItem != null) {
                ItemStack finalStack = ammoItem instanceof GenerateAmmo ? ammoItem.getDefaultInstance() : takeAmmo;
                finalStack.setCount(takeAmmo.getCount());
                return finalStack;
            }
        }
        return takeAmmo;
    }

    @ModifyVariable(method = "getTooltipImage",
            at = @At("STORE"),
            name = "ammoStack", remap = true)
    public ItemStack replaceAmmoStack(ItemStack ammoStack, ItemStack stack) {
        AmmoInjector.AmmoContext context = AmmoInjector.get().readCxtFromStack(stack);
        if (!context.isEmpty()) {
            String variantId = context.caliber().variant();
            Item ammoItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(variantId));
            return ammoItem instanceof GenerateAmmo ? ammoItem.getDefaultInstance() : ammoStack;
        }
        return ammoStack;
    }
}
