package net.zerocontact.entity;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.item.AmmoItem;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
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
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.entity.ai.goal.PerformGunAttackGoal;
import net.zerocontact.registries.ModSoundEventsReg;
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

public class ArmedRaider extends PatrollingMonster implements GeoEntity, RangedAttackMob, InventoryCarrier {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().then("raider.animation.alert", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private final RandomSource random = this.getRandom();
    private final SimpleContainer inventory = new SimpleContainer(5);
    private final LazyOptional<IItemHandler> itemHandlerLazyOptional = LazyOptional.of(() -> new InvWrapper(inventory));
    private final IGunOperator operator;

    private enum SoundState {
        IDLE,
        CONTACT,
        RELOAD
    }

    enum Weapon {
        AK(),
        SKS("tacz:sks_tactical", "tacz:762x39"),
        M4("tacz:m4a1", "tacz:556x45"),
        SCAR_L("tacz:scar_l", "tacz:556x45");
        private String gunName;
        private String ammoName;

        Weapon() {
        }

        Weapon(String gunName, String ammoName) {
            this.gunName = gunName;
            this.ammoName = ammoName;
        }

        @NotNull ItemStack getWeapon() {
            AbstractGunItem gun = (AbstractGunItem) ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz", "modern_kinetic_gun"));
            assert gun != null;
            ItemStack gunStack = new ItemStack(gun);
            gun.setGunId(gunStack, new ResourceLocation(Objects.requireNonNullElse(gunName, "tacz:ak47")));
            gun.hasBulletInBarrel(gunStack);
            gun.setFireMode(gunStack, FireMode.AUTO);
            gun.setCurrentAmmoCount(gunStack, 30);
            return gunStack;
        }

        @NotNull ItemStack getAmmo() {
            CompoundTag tag = new CompoundTag();
            AmmoItem ammo = (AmmoItem) ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz", "ammo"));
            tag.putString("AmmoId", Objects.requireNonNullElse(ammoName, "tacz:762x39"));
            assert ammo != null;
            ItemStack ammoStack = new ItemStack(ammo, 30);
            ammoStack.setTag(tag);
            return ammoStack;
        }

    }

    private Weapon randomEnum() {
        Weapon[] values = Weapon.values();
        return values[random.nextInt(values.length)];
    }

    private SoundState currentState = SoundState.IDLE;
    private SoundState lastState = SoundState.IDLE;
    private boolean soundPlayedState = false;

    public ArmedRaider(EntityType<? extends PatrollingMonster> entityType, Level level) {
        super(entityType, level);
        operator = IGunOperator.fromLivingEntity(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HurtByTargetGoal(this, Monster.class));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 5, 1.0F, 1.5F));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, IronGolem.class, 10, 1.0F, 1.5F));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0F));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 15.0F, 0.5F));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0f, 10, 30, 30));
        this.goalSelector.addGoal(4, new PerformGunAttackGoal(this, 10));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, IronGolem.class, true, false));

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.MAX_HEALTH, 30.0F)
                .add(Attributes.ATTACK_DAMAGE, 3.0F)
                .add(Attributes.FOLLOW_RANGE, 25.0F);
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
        Weapon weapon = randomEnum();
        this.setItemInHand(InteractionHand.MAIN_HAND, weapon.getWeapon());
        finalizeInvs(weapon);
        operator.initialData();
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    public void finalizeInvs(Weapon weapon) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            inventory.addItem(weapon.getAmmo());
        }
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource damageSource, int looting, boolean hitByPlayer) {
        ItemStack mainHandItem = getMainHandItem();
        if (mainHandItem.isEmpty()) return;
        if (random.nextFloat() < .3F) {
            this.spawnAtLocation(mainHandItem.copy());
        }
        Containers.dropContents(this.level(), this.getOnPos(), randomAddLootsList());
        super.dropCustomDeathLoot(damageSource, looting, hitByPlayer);
    }

    private @NotNull NonNullList<ItemStack> randomAddLootsList() {
        NonNullList<ItemStack> stackList = NonNullList.create();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (random.nextFloat() <= .3F && !inventory.getItem(i).isEmpty()) {
                stackList.add(inventory.getItem(i).copy());
            }
        }
        return stackList;
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float velocity) {
        triggerAnim("shoot_controller", "shoot");
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        this.writeInventoryToTag(compound);
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.readInventoryFromTag(compound);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, final @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerLazyOptional.cast();
        }
        return super.getCapability(cap, null);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerLazyOptional.invalidate();
    }

    private void listenSoundState() {
        getSoundState();
        if (currentState != lastState) {
            soundPlayedState = false;
            lastState = currentState;
        }
        if (!soundPlayedState) {
            switch (currentState) {
                case CONTACT -> {
                    this.playSound(ModSoundEventsReg.RAIDER_CONTACT, 1, 1);
                    soundPlayedState = true;
                }
                case RELOAD -> {
                    this.playSound(ModSoundEventsReg.RAIDER_RELOAD, 1, 1);
                    soundPlayedState = true;
                }
            }
        }
    }

    private void getSoundState() {
        if (this.getTarget() != null) {
            currentState = SoundState.CONTACT;
        } else if (this.random.nextInt(200) == 0) {
            currentState = SoundState.IDLE;
        } else {
            currentState = SoundState.IDLE;
        }
        if (operator.getSynReloadState().getStateType().isReloading()) {
            currentState = SoundState.RELOAD;
        }
    }


    @Override
    public void tick() {
        super.tick();
        listenSoundState();
    }


    @Override
    public boolean isAlliedTo(@NotNull Entity entity) {
        if (entity instanceof ArmedRaider) {
            return true;
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public @NotNull SimpleContainer getInventory() {
        return this.inventory;
    }
}
