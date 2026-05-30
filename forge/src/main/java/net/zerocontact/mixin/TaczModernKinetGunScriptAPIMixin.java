package net.zerocontact.mixin;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.events.EventUtil;
import net.zerocontact.network.ServerAmmoSelector;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModernKineticGunScriptAPI.class)
public class TaczModernKinetGunScriptAPIMixin {
    @Shadow(remap = false)
    private LivingEntity shooter;
    @Shadow(remap = false)
    private AbstractGunItem abstractGunItem;
    @Shadow(remap = false)
    private ItemStack itemStack;

    @Inject(method = "consumeAmmoFromPlayer", at = @At("HEAD"), remap = false, cancellable = true)
    public void zeroContact$consumeAmmoFromPlayerRigs(int neededAmount, CallbackInfoReturnable<Integer> cir) {
        ItemStack rigs = EventUtil.getCuriosStackFirst(shooter, "rigs");
        if (rigs.isEmpty()) {
            shooter.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(itemHandler -> zeroContact$extractDuringReload(neededAmount, cir, itemHandler, null));
        } else {
            rigs.getCapability(ForgeCapabilities.ITEM_HANDLER, null).map(itemHandler -> zeroContact$extractDuringReload(neededAmount, cir, itemHandler, rigs));
        }
        cir.cancel();
    }

    @Unique
    private int zeroContact$extractDuringReload(int neededAmount, CallbackInfoReturnable<Integer> cir, IItemHandler itemHandler, @Nullable ItemStack rigs) {
        int ammoCount = 0;
        CompoundTag gunTag = itemStack.getTag();
        if (gunTag != null) {
            CompoundTag aiAmmo = gunTag.getCompound("ai_ammo");
            IItemHandler modifiedHandler = ServerAmmoSelector.filteredAmmoHandler(itemHandler, aiAmmo.getString("selected_variant"));
            ammoCount = zeroContact$getAmmoCount(modifiedHandler, ammoCount, rigs);
            int actualNeededAmount = zeroContact$checkDropAmmo(neededAmount);
            zeroContact$extractAmmo(itemStack, aiAmmo.getString("selected_variant"), actualNeededAmount, cir, itemHandler, modifiedHandler, ammoCount, rigs);
        }
        return ammoCount;
    }

    @Unique
    private void zeroContact$extractAmmo(ItemStack gunStack, String selectedVariant, int neededAmount, CallbackInfoReturnable<Integer> cir, IItemHandler itemHandler, IItemHandler modifiedHandler, int ammoCount, @Nullable ItemStack rigs) {
        int result = this.abstractGunItem.findAndExtractInventoryAmmo(modifiedHandler, itemStack, neededAmount);
        if (itemHandler instanceof ItemStackHandler itemStackHandler && rigs != null) {
            rigs.getOrCreateTag().put("inventory", itemStackHandler.serializeNBT().getList("Items", Tag.TAG_COMPOUND));
        }
        gunStack.getOrCreateTag().getCompound("ai_ammo").putString("existed_variant", selectedVariant);
        cir.setReturnValue(result);
    }

    @Unique
    private int zeroContact$checkDropAmmo(int neededAmount){
        String clientSelected = itemStack.getOrCreateTag().getCompound("ai_ammo").getString("selected_variant");
        if(clientSelected.isEmpty())return neededAmount;
        Item selectedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(clientSelected));
        if(selectedItem ==null)return neededAmount;
        return ServerAmmoSelector.dropAmmoFromGun(shooter,itemStack,new ItemStack(selectedItem),neededAmount);
    }
    @Unique
    private int zeroContact$getAmmoCount(IItemHandler itemHandler, int ammoCount, ItemStack rigs) {
        for (int i = 0; i < itemHandler.getSlots(); ++i) {
            ItemStack checkAmmoStack = itemHandler.getStackInSlot(i);
            Item boxAmmoCount = checkAmmoStack.getItem();
            if (boxAmmoCount instanceof IAmmo iAmmo) {
                if (iAmmo.isAmmoOfGun(itemStack, checkAmmoStack)) {
                    ammoCount += checkAmmoStack.getCount();
                }
            }
        }
        return ammoCount;
    }
}
