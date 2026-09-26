package net.zerocontact.mixin.tacz;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.caliber.ICartridgeHolder;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.compat.ReloadManager;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.compat.MagazinesCompatHandler;
import net.zerocontact.caliber.ServerAmmoSelector;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mixin(ModernKineticGunScriptAPI.class)
public abstract class ModernKineticGunScriptAPIMixin {
    @Shadow(remap = false)
    private LivingEntity shooter;
    @Shadow(remap = false)
    private AbstractGunItem abstractGunItem;
    @Shadow(remap = false)
    private ItemStack itemStack;

    @Unique
    private final Supplier<ReloadManager.ReloadInventory> zeroContact$reloadInventory = () -> ReloadManager.resolveReloadInv(shooter);

    @Unique
    private boolean zeroContact$hasSelectedAmmoForReload;
    @Unique
    private boolean zeroContact$shouldRestoreChamber;

    @Inject(method = "consumeAmmoFromPlayer", at = @At("HEAD"), remap = false, cancellable = true)
    public void zeroContact$consumeAmmoFromPlayerRigs(int neededAmount, CallbackInfoReturnable<Integer> cir) {
        if (!(shooter instanceof ServerPlayer player)) return;
        ReloadManager.ReloadInventory reloadInventory = zeroContact$reloadInventory.get();
        IItemHandler handler = reloadInventory.rawHandler();
        ItemStack rigs = null;
        if (player.isCreative()) {
            handler = zeroContact$getCreativeHandler(handler);
        } else if (reloadInventory.source() == ReloadManager.ReloadSource.RIGS) {
            rigs = reloadInventory.containerStack();
        }

        zeroContact$extractSyncTag(neededAmount, cir, handler, rigs);
        if (rigs != null) {
            // Resolving the capability again reloads old NBT and discards the extraction.
            reloadInventory.save().run();
        }
        cir.cancel();
    }

    @Inject(method = "hasAmmoToConsume", at = @At("RETURN"), remap = false, cancellable = true)
    private void zeroContact$hasAmmoToConsume(CallbackInfoReturnable<Boolean> cir) {
        this.zeroContact$hasSelectedAmmoForReload = false;
        if (shooter instanceof ServerPlayer player && player.isCreative()) {
            cir.setReturnValue(true);
            return;
        }
        if (this.abstractGunItem.useDummyAmmo(this.itemStack)) {
            cir.setReturnValue(this.abstractGunItem.getDummyAmmoAmount(this.itemStack) > 0);
            return;
        }

        LazyOptional<ICartridgeHolder> gunCartridgeHolder = itemStack.getCapability(CapabilityRegistries.CARTRIDGE);
        String selectedVariant = gunCartridgeHolder.map(cap -> cap.getClientSelectedAmmoVariant(itemStack)).orElse("");
        IItemHandler filteredHandler = ServerAmmoSelector.filteredAmmoHandler(
                zeroContact$reloadInventory.get().rawHandler(), selectedVariant, itemStack);
        boolean hasSelectedAmmo = zeroContact$getAmmoCount(filteredHandler) > 0
                || MagazinesCompatHandler.get().getCompat()
                .map(compat -> compat.hasUsableMagazine(filteredHandler, itemStack))
                .orElse(false);
        this.zeroContact$hasSelectedAmmoForReload = hasSelectedAmmo;
        cir.setReturnValue(hasSelectedAmmo);
    }

    @Inject(method = "isReloadingNeedConsumeAmmo", at = @At("RETURN"), remap = false, cancellable = true)
    private void zeroContact$creativeConsumeFakeAmmo(CallbackInfoReturnable<Boolean> cir) {
        if (abstractGunItem.useInventoryAmmo(itemStack)) return;
        cir.setReturnValue(true);
    }



    @Unique
    private IItemHandler zeroContact$getCreativeHandler(IItemHandler rawHandler) {
        if (!(shooter instanceof ServerPlayer player)) return new ItemStackHandler(0);
        LinkedHashMap<ItemStack, Integer> ammoWrap = ServerAmmoSelector.getCandidates(player);
        NonNullList<ItemStack> stackNonNullList = ammoWrap.keySet().stream().peek(stack -> stack.setCount(stack.getMaxStackSize())).collect(Collectors.toCollection(NonNullList::create));
        if (stackNonNullList.isEmpty()) return new ItemStackHandler(0);
        if (MagazinesCompatHandler.get().getCompat().map(compat -> compat.isMagazineCompatibleWithGun(itemStack)).orElse(false)) {
            return rawHandler;
        }
        return new ItemStackHandler(stackNonNullList);
    }

    @Unique
    private int zeroContact$checkDropAmmo(int neededAmount, @Nullable ItemStack rigs) {
        ICartridgeHolder cap = itemStack.getCapability(CapabilityRegistries.CARTRIDGE).resolve().orElse(null);
        if (cap == null) return neededAmount;
        if (MagazinesCompatHandler.get().getCompat().map(compat -> compat.isMagazineCompatibleWithGun(itemStack)).orElse(false))
            return neededAmount;
        String clientSelected = cap.getClientSelectedAmmoVariant(itemStack);
        if (clientSelected.isEmpty()) return neededAmount;
        Item selectedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(clientSelected));
        if (selectedItem == null) return neededAmount;
        int currentAmmoCount = this.abstractGunItem.getCurrentAmmoCount(itemStack);
        boolean hadAmmoInBarrel = this.abstractGunItem.hasBulletInBarrel(itemStack);
        boolean includeBarrelAmmo = this.zeroContact$hasSelectedAmmoForReload && hadAmmoInBarrel;
        if (includeBarrelAmmo) {
            this.abstractGunItem.setCurrentAmmoCount(itemStack, currentAmmoCount + 1);
            this.abstractGunItem.setBulletInBarrel(itemStack, false);
        }
        int adjustedAmount = ServerAmmoSelector.dropAmmoFromGun(shooter, itemStack, new ItemStack(selectedItem), neededAmount, rigs);
        if (adjustedAmount != neededAmount) {
            this.zeroContact$shouldRestoreChamber = includeBarrelAmmo;
            return adjustedAmount;
        }
        if (includeBarrelAmmo) {
            this.abstractGunItem.setCurrentAmmoCount(itemStack, currentAmmoCount);
            this.abstractGunItem.setBulletInBarrel(itemStack, true);
        }
        return adjustedAmount;
    }

    @Unique
    private void zeroContact$extractSyncTag(
            int neededAmount,
            CallbackInfoReturnable<Integer> cir,
            IItemHandler itemHandler,
            @Nullable ItemStack rigs) {
        ICartridgeHolder cap = itemStack.getCapability(CapabilityRegistries.CARTRIDGE).resolve().orElse(null);
        if (cap == null) return;

        int actualNeededAmount = zeroContact$checkDropAmmo(neededAmount, rigs);
        String selectedVariant = cap.getClientSelectedAmmoVariant(itemStack);
        IItemHandler filteredHandler = ServerAmmoSelector.filteredAmmoHandler(itemHandler, selectedVariant, itemStack);
        zeroContact$extractAmmo(cap, selectedVariant, neededAmount, actualNeededAmount, cir, filteredHandler, rigs);
    }

    @Unique
    private int zeroContact$getAmmoCount(IItemHandler itemHandler) {
        int ammoCount = 0;
        for (int i = 0; i < itemHandler.getSlots(); ++i) {
            ItemStack ammoStack = itemHandler.getStackInSlot(i);
            Item ammoItem = ammoStack.getItem();
            if (ammoItem instanceof IAmmo ammo && ammo.isAmmoOfGun(itemStack, ammoStack)) {
                ammoCount = zeroContact$saturatedAdd(ammoCount, ammoStack.getCount());
            }
            if (ammoItem instanceof IAmmoBox ammoBox && ammoBox.isAmmoBoxOfGun(itemStack, ammoStack)) {
                ammoCount = zeroContact$saturatedAdd(ammoCount, ammoBox.getAmmoCount(ammoStack));
            }
        }
        return ammoCount;
    }

    @Unique
    private int zeroContact$saturatedAdd(int currentAmount, int addedAmount) {
        if (addedAmount <= 0) return currentAmount;
        if (currentAmount >= Integer.MAX_VALUE - addedAmount) return Integer.MAX_VALUE;
        return currentAmount + addedAmount;
    }


    @Unique
    private void zeroContact$setVariantFromMag(ItemStack magStack, ICartridgeHolder cap) {
        MagazinesCompatHandler.get().getCompat().ifPresent(compat -> compat.setVariantFromMag(itemStack, magStack, cap));
    }

    @Unique
    private void zeroContact$extractAmmo(
            ICartridgeHolder cap,
            String selectedVariant,
            int requestedAmount,
            int extractionAmount,
            CallbackInfoReturnable<Integer> cir,
            IItemHandler filteredHandler,
            @Nullable ItemStack rigs) {
        if (shooter instanceof ServerPlayer player) {
            cap.setCreativeHandling(player.isCreative());
        }

        cap.setAmmoVariantInGun(itemStack, selectedVariant);
        zeroContact$remapSelectedMagazineSlot(itemStack, filteredHandler);
        int extractedAmount = this.abstractGunItem.findAndExtractInventoryAmmo(filteredHandler, itemStack, extractionAmount);
        int amountForCaller = extractedAmount;
        if (extractionAmount > requestedAmount) {
            amountForCaller = Math.min(extractedAmount, requestedAmount);
            int replacedAmmoAmount = extractedAmount - amountForCaller;
            if (this.zeroContact$shouldRestoreChamber && replacedAmmoAmount > 0) {
                this.abstractGunItem.setBulletInBarrel(itemStack, true);
                replacedAmmoAmount--;
            }
            this.abstractGunItem.setCurrentAmmoCount(itemStack, replacedAmmoAmount);
        }
        this.zeroContact$hasSelectedAmmoForReload = false;
        this.zeroContact$shouldRestoreChamber = false;
        ItemStack changedMagStack = MagazinesCompatHandler.get().getCompat().map(compat -> compat.getMag(itemStack)).orElse(ItemStack.EMPTY);
        if (!changedMagStack.isEmpty()) {
            zeroContact$setVariantFromMag(changedMagStack, cap);
        }
        cir.setReturnValue(amountForCaller);
    }

    @Unique
    private static void zeroContact$remapSelectedMagazineSlot(
            ItemStack gunStack,
            IItemHandler handler
    ) {
        CompoundTag tag = gunStack.getTag();
        if (tag == null || !tag.contains("TaCZMag_SelectedSlot", Tag.TAG_INT)) {
            return;
        }

        if (!(handler instanceof ServerAmmoSelector.MappedItemHandler mapped)) {
            return;
        }

        int rawSlot = tag.getInt("TaCZMag_SelectedSlot");
        int filteredSlot = mapped.toFilteredSlot(rawSlot);

        if (filteredSlot >= 0) {
            tag.putInt("TaCZMag_SelectedSlot", filteredSlot);
        } else {
            tag.remove("TaCZMag_SelectedSlot");
        }
    }

    @Redirect(
            method = "shootOnce",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tacz/guns/item/ModernKineticGunScriptAPI;modifyProperty(Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false,
                    ordinal = 2
            )
    )
    public Object overrideBulletAmount(ModernKineticGunScriptAPI instance, String id, Class<?> type, Object value) {
        AmmoInjector.AmmoContext context = AmmoInjector.read(itemStack);
        if (!context.isEmpty()) {
            return context.caliber().bulletAmount();
        }
        return value;
    }
}
