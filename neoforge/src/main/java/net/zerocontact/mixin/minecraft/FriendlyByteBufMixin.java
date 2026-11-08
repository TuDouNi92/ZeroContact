package net.zerocontact.mixin.minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.armor.modular.service.ModuleStackSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin {
    // Forge-added method: both normal S2C and creative C2S item packets pass here.
    @WrapOperation(
            method = "writeItemStack(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/network/FriendlyByteBuf;",
            remap = false,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;writeNbt(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/network/FriendlyByteBuf;",
                    remap = true)
    )
    private FriendlyByteBuf zerocontact$writeModules(FriendlyByteBuf buffer, CompoundTag tag,
                                                    Operation<FriendlyByteBuf> original,
                                                    @Local(argsOnly = true) ItemStack stack) {
        return original.call(buffer, ModuleStackSync.write(stack, tag));
    }

    @Inject(method = "readItem", at = @At("RETURN"))
    private void zerocontact$readModules(CallbackInfoReturnable<ItemStack> cir) {
        ModuleStackSync.read(cir.getReturnValue());
    }
}
