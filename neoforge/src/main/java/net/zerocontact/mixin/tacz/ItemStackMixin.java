package net.zerocontact.mixin.tacz;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.api.armor.IEquipmentTypeTag;
import net.zerocontact.caliber.api.ICartridgeHolder;
import net.zerocontact.caliber.api.ICartridgeHolderAccess;
import net.zerocontact.caliber.capability.GunCartridgeTypeCap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ICartridgeHolderAccess {

    @Unique
    private ICartridgeHolder zeroContact$cartridgeHolder;

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract int getMaxDamage();

    @Shadow
    public abstract int getDamageValue();

    @Shadow
    public abstract void setDamageValue(int damage);


    @Override
    public ICartridgeHolder zeroContact$getCartridgeHolder() {
        if(zeroContact$cartridgeHolder==null){
            zeroContact$cartridgeHolder = new GunCartridgeTypeCap();
        }
        return zeroContact$cartridgeHolder;
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            cancellable = true)
    public void hurtAndBreakConditionally(int j, ServerLevel arg, LivingEntity arg2, Consumer<Item> consumer, CallbackInfo ci) {
        Item item = this.getItem();
        if (!(item instanceof ICombatArmorItem)) return;
        if (item instanceof IEquipmentTypeTag tag && tag.getArmorType().equals(IEquipmentTypeTag.EquipmentType.PLATE))
            return;
        int remainValue = (this.getMaxDamage() - this.getDamageValue());
        if (remainValue - j <= 0) {
            this.setDamageValue(this.getMaxDamage() - 1);
        }
        ci.cancel();
    }


}
