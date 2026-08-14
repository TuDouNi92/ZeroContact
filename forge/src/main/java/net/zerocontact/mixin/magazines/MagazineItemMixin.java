package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.item.MagazineAmmoSource;
import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.magazine.MagazineFamilySystem;
import com.tacz.guns.api.DefaultAssets;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static com.raiiiden.taczmagazines.item.MagazineItem.getMagazineFamilyId;

@Mixin(value = MagazineItem.class)
public abstract class MagazineItemMixin {
    @Shadow(remap = false)
    public abstract int getAmmoCount(ItemStack magazine);


    @Inject(method = "overrideStackedOnOther",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/raiiiden/taczmagazines/item/MagazineItem;setAmmoId(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/resources/ResourceLocation;)V",
                    remap = false),
            cancellable = true
    )

    //Called when holds the magazine and right-clicks on ammo
    public void overrideStackedOnOtherLoad(ItemStack stack, Slot slot, ClickAction action, Player player, CallbackInfoReturnable<Boolean> cir) {
        zeroContact$updateCartridge(stack, slot, cir);
    }

    @Unique
    private void zeroContact$updateCartridge(ItemStack mag, Slot slot, CallbackInfoReturnable<Boolean> cir) {

        ItemStack other = slot.getItem();
        String familyId = getMagazineFamilyId(mag);
        ResourceLocation familyAmmo = MagazineFamilySystem.getAmmoTypeForFamily(familyId);
        if (familyAmmo == null) {
            cir.setReturnValue(false);
            return;
        }
        ResourceLocation ammoId = MagazineAmmoSource.compatibleAmmoId(other, familyAmmo);
        if (ammoId == null || ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
            cir.setReturnValue(false);
            return;
        }

        AmmoInjector.AmmoContext contextFromAmmo = AmmoInjector.read(slot.getItem());
        if (getAmmoCount(mag) <= 0) {
            AmmoInjector.write(contextFromAmmo, mag);
        }
        AmmoInjector.AmmoContext contextFromMag = AmmoInjector.read(mag);
        if (!contextFromAmmo.caliber().equals(contextFromMag.caliber())) {
            cir.cancel();
        }
    }


    @ModifyVariable(
            method = "overrideOtherStackedOnMe",
            at = @At("STORE"),
            name = "heldAmmoId",
            remap = false
    )
    private ResourceLocation useHeldAmmoId(
            ResourceLocation heldAmmoId,
            ItemStack magazine,
            ItemStack heldStack,
            Slot slot,
            ClickAction action,
            Player player,
            SlotAccess heldAccess
    ) {
        if (!player.getAbilities().instabuild) {
            return heldAmmoId;
        }

        String familyId = getMagazineFamilyId(magazine);
        ResourceLocation familyAmmo =
                MagazineFamilySystem.getAmmoTypeForFamily(familyId);

        ResourceLocation actual =
                MagazineAmmoSource.compatibleAmmoId(heldStack, familyAmmo);

        return actual != null
                && !actual.equals(DefaultAssets.EMPTY_AMMO_ID)
                ? actual
                : heldAmmoId;
    }

    @Inject(method = "overrideOtherStackedOnMe",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/raiiiden/taczmagazines/item/MagazineItem;getMaxCapacity(Lnet/minecraft/world/item/ItemStack;)I",
                    remap = false),
            cancellable = true)
    public void overrideOtherStackedOnMeLeftLoad(ItemStack magazine, ItemStack heldStack, Slot slot, ClickAction action, Player player, SlotAccess heldAccess, CallbackInfoReturnable<Boolean> cir) {
        zeroContact$updateCartridge(magazine, heldStack, cir);
    }

    @Unique
    private void zeroContact$updateCartridge(ItemStack mag, ItemStack ammoStack, CallbackInfoReturnable<Boolean> cir) {

        String familyId = getMagazineFamilyId(mag);
        ResourceLocation familyAmmo = MagazineFamilySystem.getAmmoTypeForFamily(familyId);
        if (familyAmmo == null) {
            cir.setReturnValue(false);
            return;
        }
        ResourceLocation ammoId = MagazineAmmoSource.compatibleAmmoId(ammoStack, familyAmmo);
        if (ammoId == null || ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
            cir.setReturnValue(false);
            return;
        }


        AmmoInjector.AmmoContext contextFromAmmo = AmmoInjector.read(ammoStack);
        if (getAmmoCount(mag) <= 0) {
            AmmoInjector.write(contextFromAmmo, mag);
        }
        AmmoInjector.AmmoContext contextFromMag = AmmoInjector.read(mag);
        if (!contextFromAmmo.caliber().equals(contextFromMag.caliber())) {
            cir.cancel();
        }
    }

    @Inject(method = "appendHoverText",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    ordinal = 0,
                    shift = At.Shift.AFTER))
    public void appendCartridgeText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        AmmoInjector.AmmoContext context = AmmoInjector.read(stack);
        if (context.isEmpty()) return;
        String variantId = context.caliber().variant();
        Item ammoItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(variantId));
        if (ammoItem == null) return;
        MutableComponent ammoLabel = Component.translatable("tooltip.zerocontact.gun.ammoVariant").withStyle(ChatFormatting.GOLD).append(":");
        Component ammoDescription = Component.literal("\uD83E\uDC35 ").append(Component.translatable(ammoItem.getDefaultInstance().getDescriptionId())).withStyle(ChatFormatting.YELLOW);
        if (!(ammoItem instanceof GenerateAmmo))
            ammoDescription = Component.translatable("hud.zerocontact.ammo.default").withStyle(ChatFormatting.YELLOW);
        ammoLabel.append(ammoDescription);
        tooltip.add(ammoLabel);
    }
}
