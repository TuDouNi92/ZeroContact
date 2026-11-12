package net.zerocontact.caliber.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.CaliberHelper;
import org.jetbrains.annotations.Nullable;

public interface AmmoInjectService {

     record AmmoContext(CaliberHelper.Caliber caliber) {
        public boolean isEmpty() {
            return caliber == null || caliber.id().isEmpty() || caliber.variant().isEmpty();
        }
    }

    void writeCxtToStack(AmmoInjector.AmmoContext context, ItemStack stack);

    AmmoInjector.AmmoContext readCxtFromStack(ItemStack stack);

    void syncTagWithCartridge(CaliberHelper.Caliber defaultCaliber, ItemStack gun);

    ResourceLocation getDefaultAmmoIDFromGun(ItemStack gunStack);

    @Nullable AmmoInjector.AmmoContext setDefaultAmmoVariantInGun(ItemStack gunStack);

    void setAmmoVariantInGun(ItemStack gunStack, String selectedVariant);

    @Nullable Item getItemForVariant(AmmoInjector.AmmoContext context);

    void prepareGunCtxForEntity(LivingEntity entity);

    void setVariantForEntity(ItemStack gunStack, Item ammo);

    @Nullable AmmoContext setPlayerGunContext(ServerPlayer player);

    String getAmmoIdentity(ItemStack stack);

}
