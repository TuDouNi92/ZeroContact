package net.zerocontact.entity;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.ZeroContactLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;

public class ArmedRaider extends PatrollingMonster implements GeoEntity, RangedAttackMob {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().then("raider.animation.alert", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private final RandomSource random = this.getRandom();
    private IGunOperator operator = IGunOperator.fromLivingEntity(this);
    private int shootCoolDown = 0;
    private int burstInterval = 0;

    public ArmedRaider(EntityType<? extends PatrollingMonster> entityType, Level level) {
        super(entityType, level);
        Objects.requireNonNull(this.getAttribute(Attributes.FOLLOW_RANGE)).setBaseValue(25D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HurtByTargetGoal(this, Monster.class));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 5, 1.0F, 1.5F));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0F));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 15.0F, 0.5F));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0f, 10, 30, 30));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, IronGolem.class, true, false));

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.MAX_HEALTH, 30.0F)
                .add(Attributes.ATTACK_DAMAGE, 3.0F)
                .add(Attributes.FOLLOW_RANGE, 15.0F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 10, this::predicate));
        controllerRegistrar.add(new AnimationController<>(this, "shoot_controller", animationState -> PlayState.STOP)
                .triggerableAnim("shoot", SHOOT_ANIM));

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }


    private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> state) {
        if (state.isMoving()) {
            state.getController().setAnimation(RawAnimation.begin().then("raider.animation.walk", Animation.LoopType.LOOP));
        } else {
            state.getController().setAnimation(RawAnimation.begin().then("raider.animation.idle", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        Item gun = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz", "modern_kinetic_gun"));
        ItemStack gunStack = new ItemStack(gun);
        CompoundTag tag = new CompoundTag();
        tag.putByte("HasBulletInBarrel", (byte) 1);
        tag.putString("GunId", "tacz:ak47");
        tag.putString("GunFireMode", "AUTO");
        tag.putInt("GunCurrentAmmoCount", 120);
        gunStack.setTag(tag);
        this.setItemInHand(InteractionHand.MAIN_HAND, gunStack);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource damageSource, int looting, boolean hitByPlayer) {
        ItemStack mainHandItem = getMainHandItem();
        if (mainHandItem.isEmpty()) return;
        if (random.nextFloat() < .3F) {
            this.spawnAtLocation(mainHandItem.copy());
        }
        super.dropCustomDeathLoot(damageSource, looting, hitByPlayer);
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float velocity) {
        triggerAnim("shoot_controller", "shoot");
    }

    private IGunOperator shoot(LivingEntity target) {
        double x = target.getX() - this.getX();
        double y = target.getEyeY() - this.getEyeY();
        double z = target.getZ() - this.getZ();
        float spread = 10;
        float yaw = (float) -Math.toDegrees(Math.atan2(x, z)) + Mth.randomBetween(random, -spread, spread);
        float pitch = (float) -Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z))) + Mth.randomBetween(random, -spread, spread);
        if (!IGun.mainhandHoldGun(this)) return null;
        ShootResult result = operator.shoot(() -> pitch, () -> yaw);
        if (result == ShootResult.NOT_DRAW) {
            operator.draw(this::getMainHandItem);
            return operator;
        }
        if (result == ShootResult.NO_AMMO) {
            operator.reload();
        }
        return operator;
    }

    @Override
    public void tick() {
        super.tick();
        burstFire();
    }

    private void burstFire() {
        if (operator == null) {
            return;
        }
        if (getTarget() != null) {
            if (shootCoolDown > 0) {
                shootCoolDown--;
                burstInterval = random.nextInt(15);
            } else {
                if (burstInterval > 0) {
                    this.operator = shoot(getTarget());
                    burstInterval--;
                } else {
                    shootCoolDown = 40;
                    burstInterval = random.nextInt(40);
                }
            }
        }
    }

    @Override
    public boolean isAlliedTo(@NotNull Entity entity) {
        if (entity instanceof ArmedRaider) {
            return true;
        }
        return super.isAlliedTo(entity);
    }
}
