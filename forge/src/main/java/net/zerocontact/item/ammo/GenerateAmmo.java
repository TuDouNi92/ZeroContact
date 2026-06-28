package net.zerocontact.item.ammo;

import com.tacz.guns.api.item.nbt.AmmoItemDataAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.events.AmmoInjector;
import net.zerocontact.events.CaliberVariantDamageHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

import static net.zerocontact.ZeroContact.MOD_ID;

public class GenerateAmmo extends Item implements AmmoItemDataAccessor, IEquipmentTypeTag {
    private final String ammoId;
    private final String ammoVariant;
    private final float baseDamageFactor;
    private final int penetrateClass;
    private final float fleshDamage;
    private final float armorDamage;
    private final int stackSize;
    private final int[] tracerColor;

    public GenerateAmmo(String ammoId, String ammoVariant, float baseDamageFactor, int penetrateClass, float fleshDamage, float armorDamage, int stackSize, int[] tracerColor) {
        super(new Item.Properties().stacksTo(stackSize));
        this.ammoId = ammoId;
        this.ammoVariant = ammoVariant;
        this.baseDamageFactor = baseDamageFactor;
        this.penetrateClass = penetrateClass;
        this.fleshDamage = fleshDamage;
        this.armorDamage = armorDamage;
        this.stackSize = stackSize;
        this.tracerColor = tracerColor;
    }

    @Override
    public @NotNull ResourceLocation getAmmoId(ItemStack ammo) {
        AmmoInjector.write(new AmmoInjector.AmmoContext(
                new CaliberVariantDamageHelper.Caliber(ammoId, ammoVariant, baseDamageFactor, penetrateClass, fleshDamage, armorDamage, stackSize,tracerColor)
        ), ammo);
        return new ResourceLocation(ammoId);
    }

    public CaliberVariantDamageHelper.Caliber getDefualtCaliber() {
        return new CaliberVariantDamageHelper.Caliber(ammoId, MOD_ID + ":" + ammoVariant, baseDamageFactor, penetrateClass, fleshDamage, armorDamage, stackSize,tracerColor
        );
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        AmmoInjector.write(new AmmoInjector.AmmoContext(
                new CaliberVariantDamageHelper.Caliber(ammoId, ammoVariant, baseDamageFactor, penetrateClass, fleshDamage, armorDamage, stackSize,tracerColor)
        ), stack);
        return stack;
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.AMMO;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        Function<Float, Integer> decimalToPercent = (armorDamage) -> (int) Math.ceil(armorDamage * 100);
        Component penetrationHint = Component.translatable("tooltip.zerocontact.bullet_penetration")
                .append(":")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(String.valueOf(penetrateClass)).withStyle(ChatFormatting.YELLOW));
        Component armorDamageHint = Component.translatable("tooltip.zerocontact.bullet_armor_damage")
                .append(":")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(String.valueOf(decimalToPercent.apply(armorDamage))).withStyle(ChatFormatting.YELLOW).append("%"));
        Component fleshDamageHint = Component.translatable("tooltip.zerocontact.bullet_flesh_damage")
                .append(":")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(String.valueOf(fleshDamage)).withStyle(ChatFormatting.YELLOW));
        tooltipComponents.addAll(List.of(penetrationHint, armorDamageHint, fleshDamageHint));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
