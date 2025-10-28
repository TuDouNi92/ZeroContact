package net.zerocontact.mixin;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(ModernKineticGunScriptAPI.class)
public class TaczModernKinetGunScriptAPIMixin {
    @Shadow(remap = false)
    private LivingEntity shooter;
    @Shadow(remap = false)
    private AbstractGunItem abstractGunItem;
    @Shadow(remap = false)
    private ItemStack itemStack;

    @Inject(method = "consumeAmmoFromPlayer", at = @At("RETURN"), remap = false, cancellable = true)
    public void zeroContact$consumeAmmoFromPlayerRigs(int neededAmount, CallbackInfoReturnable<Integer> cir) {
        CuriosApi.getCuriosInventory(shooter).ifPresent(inv -> {
            inv.getStacksHandler("backpack").ifPresent(stacksHandler -> {
                ItemStack rigs = stacksHandler.getStacks().getStackInSlot(0);
                rigs.getCapability(ForgeCapabilities.ITEM_HANDLER, null).map(cap -> {
                    int result = this.abstractGunItem.findAndExtractInventoryAmmo(cap, itemStack, neededAmount);
                    if (cap instanceof ItemStackHandler itemStackHandler) {
                        rigs.getOrCreateTag().put("inventory", itemStackHandler.serializeNBT().getList("Items", Tag.TAG_COMPOUND));
                        cir.setReturnValue(result);
                    }
                    return result;
                });
            });
        });
    }
}
