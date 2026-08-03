package net.zerocontact.item.ammo;

import com.tacz.guns.api.item.nbt.AmmoItemDataAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.CaliberVariantDamageHelper;
import net.zerocontact.client.tooltip.BallisticToolTipComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GenerateAmmo extends Item implements AmmoItemDataAccessor, IEquipmentTypeTag {
    private final CaliberVariantDamageHelper.Caliber caliber;

    public GenerateAmmo(CaliberVariantDamageHelper.Caliber caliber) {
        super(new Item.Properties().stacksTo(caliber.stackSize()));
        this.caliber = caliber;
    }

    @Override
    public @NotNull ResourceLocation getAmmoId(ItemStack ammo) {
        AmmoInjector.write(new AmmoInjector.AmmoContext(caliber), ammo);
        return new ResourceLocation(caliber.id());
    }

    public CaliberVariantDamageHelper.Caliber getDefualtCaliber() {
        return caliber;
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        AmmoInjector.write(new AmmoInjector.AmmoContext(caliber), stack);
        return stack;
    }

    @Override
    public @NotNull IEquipmentTypeTag.EquipmentType getArmorType() {
        return EquipmentType.AMMO;
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        return Optional.of(new BallisticToolTipComponent(caliber));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        Function<Float, Integer> decimalToPercent = (armorDamage) -> (int) Math.ceil(armorDamage * 100);
        Component penetrationHint = Component.translatable("tooltip.zerocontact.bullet_penetration")
                .append(":")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(String.valueOf(caliber.penetrationClass())).withStyle(ChatFormatting.YELLOW));
        Component armorDamageHint = Component.translatable("tooltip.zerocontact.bullet_armor_damage")
                .append(":")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(String.valueOf(decimalToPercent.apply(caliber.armorDamage()))).withStyle(ChatFormatting.YELLOW).append("%"));
        Component fleshDamageHint = Component.translatable("tooltip.zerocontact.bullet_flesh_damage")
                .append(":")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(String.valueOf(caliber.fleshDamage())).withStyle(ChatFormatting.YELLOW));
        tooltipComponents.addAll(List.of(penetrationHint, armorDamageHint, fleshDamageHint));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
