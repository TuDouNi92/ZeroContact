package net.zerocontact.mixin.magazines;

import com.raiiiden.taczmagazines.client.MagazineLoadingHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.AmmoInjector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MagazineLoadingHandler.class)
public class MagazineLoadingHandlerMixin {

    @Shadow(remap = false)
    private static int containerSlot;

    @ModifyVariable(
            method = "returnCreativeInventoryRound",
            at = @At("STORE"),
            name = "bullet",
            remap = false
    )
    //This unload is triggered from  stack right-click action.
    private static ItemStack zeroContact$replaceCreativeBullet(
            ItemStack bullet,
            LocalPlayer player,
            ResourceLocation ammoId
    ) {
        // 从 MagazineLoadingHandler.containerSlot 对应的弹匣读取上下文
        ItemStack magazine = player.getInventory().getItem(containerSlot);

        AmmoInjector.AmmoContext context = AmmoInjector.read(magazine);
        if (context.isEmpty()) {
            return bullet;
        }

        Item item = AmmoInjector.getAmmoVariantItem(context);
        if (item == null) {
            return bullet;
        }

        ItemStack replacement = item.getDefaultInstance();
        replacement.setCount(bullet.getCount());
        return replacement;
    }

    @ModifyVariable(
            method = "creativeUnloadOneFromHand",
            at = @At("STORE"),
            name = "bullet",
            remap = false
    )
    //This unload is triggered in hand.
    private static ItemStack zeroContact$replaceCreativeBullet(
            ItemStack bullet,
            LocalPlayer player
    ) {
        ItemStack heldMag = player.getMainHandItem();
        AmmoInjector.AmmoContext context = AmmoInjector.read(heldMag);
        if (context.isEmpty()) {
            return bullet;
        }

        Item item = AmmoInjector.getAmmoVariantItem(context);
        if (item == null) {
            return bullet;
        }

        ItemStack replacement = item.getDefaultInstance();
        replacement.setCount(bullet.getCount());
        return replacement;
    }
}
