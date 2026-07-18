package net.zerocontact.mixin.tacz;

import com.tacz.guns.client.tooltip.ClientGunTooltip;
import com.tacz.guns.inventory.tooltip.GunTooltip;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientGunTooltip.class)
public class ClientGunToolTipMixin {
    @Mutable
    @Shadow(remap = false)
    @Final
    private ItemStack ammo;
    @Shadow(remap = false)
    @Final
    private ItemStack gun;

    @Inject(method = "<init>",at = @At("RETURN"),remap = false)
    private void replaceAmmoStack(GunTooltip tooltip, CallbackInfo ci){
        AmmoInjector.AmmoContext context = AmmoInjector.read(this.gun);
        if(!context.caliber().variant().isEmpty() && !context.caliber().variant().equals("tacz:ammo")){
            Item variantItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(context.caliber().variant()));
            if(variantItem!=null){
                this.ammo = variantItem.getDefaultInstance();
            }
        }
    }
}
