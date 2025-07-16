package net.zerocontact.item.dogtag;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.entity.ArmedRaider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static net.zerocontact.ZeroContact.MOD_ID;

public abstract class BaseDogTag extends Item implements GeoItem {

    public BaseDogTag(Properties properties) {
        super(properties);
    }

    //For normal damage source
    public static void appendInfos(ItemStack stack, @NotNull Entity killedByName, @NotNull Entity victimName, @NotNull ItemStack weapon, DamageSource source) {
        if(source.getEntity()==null){
            stack.getOrCreateTag().putString("attacker", Component.Serializer.toJson(Component.literal(source.toString())));
            stack.getOrCreateTag().putString("weapon", Component.Serializer.toJson(Component.literal(source.toString())));
        }
        else{
            stack.getOrCreateTag().putString("attacker", Component.Serializer.toJson(killedByName.getDisplayName()));

            stack.getOrCreateTag().putString("weapon", Component.Serializer.toJson(weapon.getDisplayName()));
        }
        stack.getOrCreateTag().putString("victim", Component.Serializer.toJson(victimName.getDisplayName()));
        stack.getOrCreateTag().putString("timestamp", Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime().toString());
    }

    //For projectile damage source
    public static void appendInfos(ItemStack stack, @NotNull Entity killedByName, @NotNull Entity victimName, @NotNull Entity weapon) {
        stack.getOrCreateTag().putString("attacker", Component.Serializer.toJson(killedByName.getDisplayName()));
        stack.getOrCreateTag().putString("victim", Component.Serializer.toJson(victimName.getDisplayName()));
        stack.getOrCreateTag().putString("timestamp", Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime().toString());
        if (weapon instanceof EntityKineticBullet bullet) {
            stack.getOrCreateTag().putString("weapon", Component.Serializer.toJson(Component.translatable(bullet.getGunId().toLanguageKey())));
        } else {
            stack.getOrCreateTag().putString("weapon", Component.Serializer.toJson(weapon.getDisplayName()));
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (stack.getItem() instanceof BaseDogTag) {
            MutableComponent victimName = Component.Serializer.fromJson(stack.getOrCreateTag().getString("victim"));
            String timeStamp = stack.getOrCreateTag().getString("timestamp");
            MutableComponent killedByName = Component.Serializer.fromJson(stack.getOrCreateTag().getString("attacker"));
            MutableComponent weaponName = Component.Serializer.fromJson(stack.getOrCreateTag().getString("weapon"));
            assert killedByName != null;
            assert weaponName != null;
            tooltipComponents.addAll(
                    List.of(
                            Component.translatable("item.zerocontact.dogtag.victim", victimName),
                            Component.translatable("item.zerocontact.dogtag.time_stamp", timeStamp),
                            Component.translatable("item.zerocontact.dogtag.killedBy", killedByName.withStyle(ChatFormatting.RED)),
                            Component.translatable("item.zerocontact.dogtag.weaponName", weaponName.withStyle(ChatFormatting.AQUA))
                    )
            );
        }
    }

    public static boolean onKillEntity(LivingEntity livingEntity, DamageSource damageSource) {
        if (livingEntity instanceof ServerPlayer || livingEntity instanceof ArmedRaider) {
            Item dogTag = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MOD_ID, "dog_tag"));
            if (!(dogTag instanceof BaseDogTag baseDogTag)) return true;
            Entity projectile = damageSource.getDirectEntity();
            LivingEntity attacker = (LivingEntity) damageSource.getEntity();
            ItemStack tagStack = new ItemStack(baseDogTag, 1);
            if (projectile == null) {
                Optional.ofNullable(attacker).ifPresent(__ -> BaseDogTag.appendInfos(
                        tagStack,
                        attacker,
                        livingEntity,
                        attacker.getMainHandItem(),
                        damageSource
                ));

            } else {
                assert attacker != null;
                BaseDogTag.appendInfos(
                        tagStack,
                        attacker,
                        livingEntity,
                        projectile
                );
            }
            NonNullList<ItemStack> nonNullList = NonNullList.create();
            nonNullList.add(tagStack);
            Containers.dropContents(livingEntity.level(), livingEntity.getOnPos(), nonNullList);
        }
        return false;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
