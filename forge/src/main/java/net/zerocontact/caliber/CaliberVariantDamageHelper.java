package net.zerocontact.caliber;

import com.google.common.util.concurrent.AtomicDouble;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.zerocontact.api.ICartridgeType;
import net.zerocontact.api.ICombatArmorItem;
import net.zerocontact.command.CommandManager;
import net.zerocontact.datagen.AmmoDataPOJO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public enum CaliberVariantDamageHelper {
    CALIBER_762x39(
            new Caliber("tacz:762x39", 3, 6, 5.5f)
    ),
    CALIBER_556x45(
            new Caliber("tacz:556x45", 2, 5, 4)
    ),
    CALIBER_580x42(
            new Caliber("tacz:58x42", 3, 6, 6f)
    ),
    CALIBER_308(
            new Caliber("tacz:308", 4, 10, 8)
    ),
    CALIBER_50AE(
            new Caliber("tacz:50ae", 2, 9, 6.2f)
    ),
    CALIBER_9mm(
            new Caliber("tacz:9mm", 1.5f, 4, 3.75f)
    ),
    CALIBER_45ACP(
            new Caliber("tacz:45acp", 1.25f, 5, 4)
    ),
    CALIBER_762x25(
            new Caliber("tacz:762x25", 1.5f, 3, 3f)
    ),
    CALIBER_762x54(
            new Caliber("tacz:762x54", 4, 12, 8)
    ),
    CALIBER_338(
            new Caliber("tacz:338", 3, 14, 15)
    ),
    CALIBER_68x51(
            new Caliber("tacz:68x51fury", 2.5f, 7, 6)
    ),
    CALIBER_50BMG(
            new Caliber("tacz:50bmg", 4, 18, 18.5f)
    ),
    CALIBER_12G(
            new Caliber("tacz:12g", 0.2f, 3, 1.25f)
    ),
    CALIBER_22WMR(
            new Caliber("tacz:22wmr", 1.5f, 5, 6)
    ),
    CALIBER_30_06(
            new Caliber("tacz:30_06", 4, 8, 12)
    ),
    CALIBER_46x30(
            new Caliber("tacz:46x30", 2f, 4, 5)
    ),
    CALIBER_57x28(
            new Caliber("tacz:57x28", 1.5f, 5, 6)
    ),
    CALIBER_45_70(
            new Caliber("tacz:45_70", 5f, 8, 10)
    ),
    CALIBER_357MAG(
            new Caliber("tacz:357mag", 2, 6, 7)
    ),
    CALIBER_500MAG(
            new Caliber("tacz:500mag", 3, 7, 6)
    );

    private final Caliber caliber;
    private static final EnumSet<CaliberVariantDamageHelper> caliberVariantDamageHelperEnumSet = EnumSet.allOf(CaliberVariantDamageHelper.class);
    private static final String DEFAULT = "tacz:ammo";

    CaliberVariantDamageHelper(Caliber caliber) {
        this.caliber = caliber;
    }


    /**
     * Represents the caliber infos of a type of ammo
     *
     * <p>This class is meant to create a middleware that modifies ammo damage associated with gun data</p>
     *
     * @param id               The ammo id from TAC:Z guns
     * @param baseDamageFactor The balancing factor for each caliber generating new numbers
     * @param penetrationClass The penetration level for damage interceptor, bypassed when the feature is off
     * @param fleshDamage      The flesh damage for damage interceptor, bypassed when the feature is off
     */
    public record Caliber(
            String id,
            String variant,
            int life,
            float speed,
            float friction,
            float gravity,
            float knockback,
            float recoilMultiplier,
            float inaccuracyMultiplier,
            AmmoDataPOJO.Explosion explosion,
            AmmoDataPOJO.Ignite ignite,
            float baseDamageFactor,
            int penetrationClass,
            float fleshDamage,
            float armorDamage,
            int stackSize,
            int[] tracerColor,
            EventHook[] hooks
    ) implements ICartridgeType {

        public Caliber {
            explosion = Objects.requireNonNullElse(explosion, AmmoDataPOJO.Explosion.NONE);
            ignite = Objects.requireNonNullElse(ignite, AmmoDataPOJO.Ignite.NONE);
            tracerColor = tracerColor == null ? new int[]{255, 255, 255, 255} : tracerColor.clone();
            hooks = hooks == null ? new EventHook[]{} : hooks;
        }

        //This constructor is only for enums;
        private Caliber(String id, float baseDamageFactor, int penetrationClass, float fleshDamage) {
            this(
                    id,
                    DEFAULT,
                    30,
                    1,
                    0.015f,
                    0.15f,
                    0,
                    1,
                    1,
                    AmmoDataPOJO.Explosion.NONE,
                    AmmoDataPOJO.Ignite.NONE,
                    baseDamageFactor,
                    penetrationClass,
                    fleshDamage,
                    0,
                    30,
                    new int[]{255, 255, 255, 255},
                    new EventHook[]{}
            );
        }

        public static Caliber createDefaultCaliberFromGun(String id, ItemStack gunStack) {
            ResourceLocation gunId = Optional.ofNullable(IGun.getIGunOrNull(gunStack)).map(ig -> ig.getGunId(gunStack)).orElse(new ResourceLocation(""));
            GunData gunData = TimelessAPI.getCommonGunIndex(gunId).map(CommonGunIndex::getGunData).orElse(null);
            if (gunData == null) return null;
            BulletData bulletData = gunData.getBulletData();
            return new Caliber(
                    id,
                    DEFAULT,
                    Math.round(bulletData.getLifeSecond() * 20),
                    bulletData.getSpeed(),
                    bulletData.getFriction(),
                    bulletData.getGravity(),
                    bulletData.getKnockback(),
                    1,
                    1,
                    AmmoDataPOJO.Explosion.NONE,
                    AmmoDataPOJO.Ignite.NONE,
                    0,
                    0,
                    0,
                    0,
                    30,
                    new int[]{255, 255, 255, 255},
                    new EventHook[]{}
            );
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Caliber caliber && Objects.equals(id, caliber.id) && (Objects.equals(variant, caliber.variant));
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, variant);
        }

        @Override
        public @NotNull Map<InaccuracyType, Float> getInaccuracy(ItemStack gunStack) {
            ResourceLocation gunId = Optional.ofNullable(IGun.getIGunOrNull(gunStack)).map(ig -> ig.getGunId(gunStack)).orElse(new ResourceLocation(""));
            GunData gunData = TimelessAPI.getCommonGunIndex(gunId).map(CommonGunIndex::getGunData).orElse(null);
            if (gunData != null) {
                Map<InaccuracyType, Float> inaccuracyMap = new EnumMap<>(InaccuracyType.class);
                inaccuracyMap.putAll(gunData.getInaccuracy());
                inaccuracyMap.replaceAll((__, value) -> value * inaccuracyMultiplier);
                return inaccuracyMap;
            }
            return Map.of();
        }

        @Override
        public GunRecoil getRecoil(ItemStack gunStack) {
            ResourceLocation gunId = Optional.ofNullable(IGun.getIGunOrNull(gunStack)).map(ig -> ig.getGunId(gunStack)).orElse(new ResourceLocation(""));
            GunData gunData = TimelessAPI.getCommonGunIndex(gunId).map(CommonGunIndex::getGunData).orElse(null);
            if (gunData == null) return null;
            GunRecoil gunRecoil = copyRecoil(gunData.getRecoil());
            if (gunRecoil == null) return null;
            GunRecoilKeyFrame[] yawFrame = gunRecoil.getYaw();
            GunRecoilKeyFrame[] pitchFrame = gunRecoil.getPitch();
            if (yawFrame != null && pitchFrame != null) {
                for (GunRecoilKeyFrame frame : yawFrame) {
                    frame.getValue()[0] *= recoilMultiplier;
                    frame.getValue()[1] *= recoilMultiplier;
                }

                for (GunRecoilKeyFrame frame : pitchFrame) {
                    frame.getValue()[0] *= recoilMultiplier;
                    frame.getValue()[1] *= recoilMultiplier;
                }
            }
            return gunRecoil;
        }

        private static GunRecoil copyRecoil(GunRecoil source) {
            if (source == null) return null;
            GunRecoil copy = new GunRecoil();
            copy.setYaw(copyKeyFrames(source.getYaw()));
            copy.setPitch(copyKeyFrames(source.getPitch()));
            return copy;
        }

        private static GunRecoilKeyFrame[] copyKeyFrames(GunRecoilKeyFrame[] source) {
            if (source == null) return null;
            GunRecoilKeyFrame[] copy = new GunRecoilKeyFrame[source.length];
            for (int i = 0; i < source.length; i++) {
                GunRecoilKeyFrame frame = source[i];
                if (frame == null) continue;
                GunRecoilKeyFrame frameCopy = new GunRecoilKeyFrame();
                frameCopy.setTime(frame.getTime());
                float[] value = frame.getValue();
                frameCopy.setValue(value == null ? null : value.clone());
                copy[i] = frameCopy;
            }
            return copy;
        }
    }

    /**
     * <p>This method is meant to match calibers with input bullet damage source </p>
     *
     * @param source The Minecraft damage source
     * @param set    The enum set for calibers defined
     * @param <E>    The enum set type
     * @return Return the caliber that matched with damage source
     */
    private static <E> Optional<Caliber> getMatchedCaliber(DamageSource source, Set<E> set) {
        AtomicReference<Optional<Caliber>> result = new AtomicReference<>(Optional.empty());
        if (!(source.getDirectEntity() instanceof EntityKineticBullet bullet)) return result.get();
        @Nullable AmmoInjector.AmmoContext ammoContext = BulletBinder.getContext(bullet);
        if (!source.is(ModDamageTypes.BULLETS_TAG) || ammoContext == null) return result.get();

        for (E caliberData : set) {
            if (caliberData instanceof CaliberVariantDamageHelper caliberEnum) {
                if (caliberEnum.caliber.id.equals(bullet.getAmmoId().toString())) {
                    result.set(Optional.of(caliberEnum.caliber));
                    break;
                }

            } else if (caliberData instanceof Caliber caliber) {
                if (ammoContext != null && ammoContext.isEmpty() && source.getEntity() instanceof ServerPlayer player) {
                    ammoContext = AmmoInjector.setPlayerGunContext(player);
                }
                if (ammoContext != null && caliber.equals(ammoContext.caliber())) {
                    result.set(Optional.of(caliber));
                    break;
                }
            }
        }
        return result.get();
    }

    /**
     * <p>This method is meant to generate damages under the effect of protections</p>
     *
     * @param original    The original bullet damage
     * @param source      The Minecraft damage source
     * @param hurtCanHold The damage that armor/plate can withstand
     * @param provider    Interface implementation that provides the situation of getting hit by bullets
     * @return The generated damage amount
     */
    public static float generateDamageAmount(float original, DamageSource source, int hurtCanHold, @Nullable ICombatArmorItem provider) {
        AtomicDouble output = new AtomicDouble(original);
        Optional.ofNullable(source.getDirectEntity()).ifPresent(bullet -> {
            if (bullet.level() instanceof ServerLevel serverLevel) {
                if (CommandManager.CommandSavedData.get(serverLevel).experimentalBallistic) {
                    Set<Caliber> mergedCaliberSet = caliberVariantDamageHelperEnumSet.stream().map(a -> a.caliber).collect(Collectors.toSet());
                    mergedCaliberSet.removeAll(CaliberRegistry.calibers().values());
                    mergedCaliberSet.addAll(CaliberRegistry.calibers().values());
                    getMatchedCaliber(source, mergedCaliberSet).ifPresent(caliber -> {
                        double penetratedDamage = getPenetratedDamage(caliber, hurtCanHold);
                        setOutput(provider, caliber, penetratedDamage, output);
                    });
                } else {
                    getMatchedCaliber(source, caliberVariantDamageHelperEnumSet).ifPresent(caliber -> {
                        double penetratedDamage = getPenetratedDamage(caliber, hurtCanHold);
                        setOutput(provider, caliber, penetratedDamage, output);
                    });
                }
            }

        });
        return (float) output.get();
    }

    private static void setOutput(@Nullable ICombatArmorItem provider, Caliber caliber, double penetratedDamage, AtomicDouble output) {
        if (penetratedDamage > 0) {
            if (provider == null) {
                output.set(penetratedDamage);
            } else {
                output.set(penetratedDamage * provider.generatePenetrated());
            }
        } else {
            if (provider == null) {
                output.set(caliber.fleshDamage);
            } else {
                output.set(caliber.penetrationClass * 0.3 * provider.generateBlunt());
            }
        }
    }

    /**
     * This method generates the damage once armor get penetrated
     *
     * @param caliber     Caliber class
     * @param hurtCanHold The damage that armor/plate can withstand
     * @return Determine and returns the flesh damage
     */
    private static double getPenetratedDamage(@NotNull Caliber caliber, int hurtCanHold) {
        RandomSource randomSource = RandomSource.create();
        if (hurtCanHold >= caliber.penetrationClass) {
            double preOdds = 0.42139
                    + 2.00643 * caliber.penetrationClass
                    - 1.80617 * hurtCanHold;
            double penetrateOdds = 1.0 / (1.0 + Math.exp(-preOdds));
            if (randomSource.nextFloat() < penetrateOdds) {
                return caliber.fleshDamage;
            }
            return 0.0;
        } else {
            return caliber.fleshDamage;
        }
    }
}
