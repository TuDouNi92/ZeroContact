package net.zerocontact.client.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.zerocontact.caliber.CaliberVariantDamageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AdvancedAmmoInfoComponents {
    private static final String BULLET_RECOIL_KEY = "tooltip.zerocontact.bullet_recoil";
    public static final String FLESH_DAMAGE_KEY = "tooltip.zerocontact.bullet_flesh_damage";
    public static final String BULLET_PENETRATION_KEY = "tooltip.zerocontact.bullet_penetration";
    public static final String ARMOR_DAMAGE_KEY = "tooltip.zerocontact.bullet_armor_damage";
    public static final String BULLET_INACCURACY = "tooltip.zerocontact.bullet_inaccuracy";
    public static final String BULLET_KNOCK_BACK = "tooltip.zerocontact.bullet_knockback";

    public static List<Component> create(CaliberVariantDamageHelper.Caliber caliber, boolean fromAmmo) {
        Function<Float, Component> decimalToPercentOff = (amount) -> {
            int result = Math.round((1 - amount) * 100);
            result = result < 0 ? Math.abs(result) : -result;
            return Component.literal((result >= 0 ? "+" : "") + result).append("%");
        };
        Function<Float, Component> decimalToPercent = (amount) -> Component.literal(String.valueOf(Math.round(amount * 100))).append("%");
        BiFunction<String, Component, Component> advancedAmmoInfoComponent = (path, value) -> Component.translatable(path).append(":").append(value).withStyle(ChatFormatting.DARK_GRAY);
        Component penetration = advancedAmmoInfoComponent.apply(BULLET_PENETRATION_KEY, Component.literal(String.valueOf(caliber.penetrationClass())));
        Component armorDamage = advancedAmmoInfoComponent.apply(ARMOR_DAMAGE_KEY, decimalToPercent.apply(caliber.armorDamage()));
        Component fleshDamage = advancedAmmoInfoComponent.apply(FLESH_DAMAGE_KEY, Component.literal(String.valueOf(caliber.fleshDamage())));
        Component recoil = advancedAmmoInfoComponent.apply(BULLET_RECOIL_KEY, decimalToPercentOff.apply(caliber.recoilMultiplier()));
        Component inaccuracy = advancedAmmoInfoComponent.apply(BULLET_INACCURACY, decimalToPercentOff.apply(caliber.inaccuracyMultiplier()));
        Component knockback = advancedAmmoInfoComponent.apply(BULLET_KNOCK_BACK, decimalToPercent.apply(caliber.knockback()));
        ArrayList<Component> list = new ArrayList<>(List.of(
                penetration,
                armorDamage,
                fleshDamage,
                recoil,
                inaccuracy,
                knockback
        ));
        if (fromAmmo) {
            list.removeAll(List.of(penetration, armorDamage, fleshDamage));
        }
        return list;
    }
}
