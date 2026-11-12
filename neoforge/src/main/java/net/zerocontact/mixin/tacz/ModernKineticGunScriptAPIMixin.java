package net.zerocontact.mixin.tacz;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zerocontact.caliber.api.ICartridgeHolder;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.capability.CapabilityRegistry;
import net.zerocontact.caliber.compat.ReloadManager;
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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import net.zerocontact.item.ammo.GenerateAmmo;

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

        Optional<ICartridgeHolder> gunCartridgeHolder = Optional.ofNullable(itemStack.getCapability(CapabilityRegistry.CARTRIDGE));
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
        ICartridgeHolder cap = itemStack.getCapability(CapabilityRegistry.CARTRIDGE);
        if (cap == null) return neededAmount;
        if (MagazinesCompatHandler.get().getCompat().map(compat -> compat.isMagazineCompatibleWithGun(itemStack)).orElse(false))
            return neededAmount;
        String clientSelected = cap.getClientSelectedAmmoVariant(itemStack);
        if (clientSelected.isEmpty()) return neededAmount;
        ResourceLocation selectedId = ResourceLocation.tryParse(clientSelected);
        if (selectedId == null) return neededAmount;
        Item selectedItem = BuiltInRegistries.ITEM.get(selectedId);
        if(selectedItem == Items.AIR)return neededAmount;
        ItemStack selectedAmmoStack;
        if (selectedItem instanceof GenerateAmmo) {
            selectedAmmoStack = selectedItem.getDefaultInstance();
        } else {
            selectedAmmoStack = AmmoItemBuilder.create().setId(selectedId).setCount(1).build();
        }
        int currentAmmoCount = this.abstractGunItem.getCurrentAmmoCount(itemStack);
        boolean hadAmmoInBarrel = this.abstractGunItem.hasBulletInBarrel(itemStack);
        boolean includeBarrelAmmo = this.zeroContact$hasSelectedAmmoForReload && hadAmmoInBarrel;
        if (includeBarrelAmmo) {
            this.abstractGunItem.setCurrentAmmoCount(itemStack, currentAmmoCount + 1);
            this.abstractGunItem.setBulletInBarrel(itemStack, false);
        }
        int adjustedAmount = ServerAmmoSelector.dropAmmoFromGun(shooter, itemStack, selectedAmmoStack, neededAmount, rigs);
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
        ICartridgeHolder cap = itemStack.getCapability(CapabilityRegistry.CARTRIDGE);
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
        MagazinesCompatHandler.get().getCompat().ifPresent(compat->{compat.remapSelectedMagazineSlot(itemStack, filteredHandler);});
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
        AmmoInjector.AmmoContext context = AmmoInjector.get().readCxtFromStack(itemStack);
        if (!context.isEmpty()) {
            return context.caliber().bulletAmount();
        }
        return value;
    }
}
