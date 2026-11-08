package net.zerocontact.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.HelmetInfoProvider;
import net.zerocontact.api.armor.ICombatArmorItem;
import net.zerocontact.config.ModConfigs;
import net.zerocontact.events.ResolveHitBodyPartEvent;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPart;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPartEnum;

/** Keeps optional FirstAid classes out of the damage pipeline and event API. */
@Mod.EventBusSubscriber(modid = ZeroContact.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FirstAidCompatHandler {
    private static final String MOD_ID = "firstaid";
    private static final String COMPAT_ID = "tacz_firstaid_compat";
    private FirstAidCompatHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void resolveHitBodyPart(ResolveHitBodyPartEvent.Pre event) {
        if (!ModList.get().isLoaded(MOD_ID) || !ModList.get().isLoaded(COMPAT_ID)) return;
        if (!(event.getHurtEntity() instanceof ServerPlayer player) || event.getHitPosition() == null) return;
        HitPartEnum part = FirstAidCompatCompat.resolvePart(event.getHitPosition(), player);
        if (part == null) return;

        float factor = 1f;
        if (part == HitPartEnum.ARM || part == HitPartEnum.LEG) {
            factor = ModConfigs.SERVER.firstAidLimbsFactor().get().floatValue();
        } else if (part == HitPartEnum.HEAD) {
            // Preserve the existing FirstAid head factor for combat helmets.
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.getItem() instanceof HelmetInfoProvider && helmet.getItem() instanceof ICombatArmorItem) {
                factor = ModConfigs.SERVER.firstAidHeadFactor().get().floatValue();
            }
        }
        event.setHitPart(new HitPart(part, factor));
    }
}
