package net.zerocontact.mixin.tacz;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.api.IEquipmentTypeTag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract int getDamageValue();

    @Shadow
    public abstract boolean hurt(int amount, RandomSource random, @Nullable ServerPlayer user);

    @Shadow
    public abstract void shrink(int decrement);

    @Shadow
    public abstract void setDamageValue(int damage);

    @Shadow
    public abstract int getMaxDamage();

    @Inject(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z"),
            cancellable = true
    )
    public <T extends LivingEntity> void hurtAndBreakConditionally(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        Item item = this.getItem();
        if (!(item instanceof ICombatArmorItem)) return;
        if (item instanceof IEquipmentTypeTag tag && tag.getArmorType().equals(IEquipmentTypeTag.EquipmentType.PLATE))
            return;
        int remainValue = (this.getMaxDamage() - this.getDamageValue());
        if (remainValue - amount <= 0) {
            this.setDamageValue(this.getMaxDamage() - 1);
        } else {
            if (hurt(amount, entity.getRandom(), entity instanceof ServerPlayer ? (ServerPlayer) entity : null)) {
                onBroken.accept(entity);
                this.shrink(1);
                if (entity instanceof Player) {
                    ((Player) entity).awardStat(Stats.ITEM_BROKEN.get(item));
                }
                this.setDamageValue(0);
            }
        }
        ci.cancel();
    }
}
