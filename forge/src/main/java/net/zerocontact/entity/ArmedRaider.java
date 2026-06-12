package net.zerocontact.entity;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.AmmoItem;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
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
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.entity.ai.NameList;
import net.zerocontact.entity.ai.controller.GlobalStateController;
import net.zerocontact.entity.ai.goal.AvoidGoal;
import net.zerocontact.entity.ai.goal.PerformGunAttackGoal;
import net.zerocontact.entity.ai.goal.RestrictedGoalWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Objects;

public class ArmedRaider extends PatrollingMonster implements GeoEntity, InventoryCarrier {
    private final AnimatableInstanceCache geoCache;
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().then("raider.animation.alert", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation WALK = RawAnimation.begin().then("raider.animation.walk", Animation.LoopType.LOOP);
    private static final RawAnimation IDLE = RawAnimation.begin().then("raider.animation.idle", Animation.LoopType.LOOP);
    public final RandomSource random = this.getRandom();
    private final SimpleContainer inventory = new SimpleContainer(5);
    private final LazyOptional<IItemHandler> itemHandlerLazyOptional = LazyOptional.of(() -> new InvWrapper(inventory));
    private final IGunOperator operator;
    public final GlobalStateController stateController;

    protected enum Weapon {
        AK(),
        SKS("tacz:sks_tactical", "tacz:762x39"),
        FN_FAL("tacz:fn_fal", "tacz:308"),
        M4("tacz:m4a1", "tacz:556x45"),
        SCAR_L("tacz:scar_l", "tacz:556x45"),
        M320("tacz:m320", "tacz:40mm"),
        AUG("tacz:aug", "tacz:556x45"),
        HK416D("tacz:hk416d", "tacz:556x45"),
        M16A4("tacz:m16a4", "tacz:556x45"),
        QBZ191("tacz:qbz_191", "tacz:58x42"),
        CZ75("tacz:cz75", "tacz:9mm"),
        GLOCK17("tacz:glock_17", "tacz:9mm"),
        P320("tacz:p320", "tacz:45acp"),
        P90("tacz:p90", "tacz:57x28"),
        VECTOR("tacz:vector45", "tacz:45acp"),
        UZI("tacz:uzi", "tacz:9mm");
        private String gunName;
        private String ammoName;

        Weapon() {
        }

        Weapon(String gunName, String ammoName) {
            this.gunName = gunName;
            this.ammoName = ammoName;
        }

        ItemStack getWeapon() {
            AbstractGunItem gun = (AbstractGunItem) ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz", "modern_kinetic_gun"));
            if (gun == null) return null;
            ItemStack gunStack = new ItemStack(gun);
            gun.setGunId(gunStack, new ResourceLocation(Objects.requireNonNullElse(gunName, "tacz:ak47")));
            TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack)).ifPresent(commonGunIndex -> {
                GunData gunData = commonGunIndex.getGunData();
                gun.hasBulletInBarrel(gunStack);
                gun.setFireMode(gunStack, gunData.getFireModeSet().get(0));
                gun.setCurrentAmmoCount(gunStack, gunData.getAmmoAmount());
            });
            return gunStack;
        }

        ItemStack getAmmo() {
            CompoundTag tag = new CompoundTag();
            AmmoItem ammo = (AmmoItem) ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz", "ammo"));
            tag.putString("AmmoId", Objects.requireNonNullElse(ammoName, "tacz:762x39"));
            if (ammo == null || ammoName == null) return null;
            Integer stackSize = TimelessAPI.getCommonAmmoIndex(new ResourceLocation(ammoName)).map(CommonAmmoIndex::getStackSize).orElse(1);
            ItemStack ammoStack = new ItemStack(ammo, stackSize);
            ammoStack.setTag(tag);
            return ammoStack;
        }

    }

    private Weapon randomWeapon() {
        Weapon[] values = Weapon.values();
        return values[random.nextInt(values.length)];
    }

    public ArmedRaider(EntityType<? extends PatrollingMonster> entityType, Level level) {
        super(entityType, level);
        operator = IGunOperator.fromLivingEntity(this);
        geoCache = GeckoLibUtil.createInstanceCache(this);
        NameList.setName(this, this.random);
        stateController = new GlobalStateController(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(2, new AvoidGoal(this, 5));
        this.goalSelector.addGoal(2, RestrictedGoalWrapper.create(this, new OpenDoorGoal(this, false)));
        this.goalSelector.addGoal(4, new PerformGunAttackGoal(this));
        this.goalSelector.addGoal(5, RestrictedGoalWrapper.create(this, new RandomStrollGoal(this, 0.8F)));
        this.goalSelector.addGoal(8, RestrictedGoalWrapper.create(this, new RandomLookAroundGoal(this)));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, IronGolem.class, true, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.MAX_HEALTH, 30.0F)
                .add(Attributes.ATTACK_DAMAGE, 3.0F)
                .add(Attributes.FOLLOW_RANGE, 35.0F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 10, this::defaultPredicate));
        controllerRegistrar.add(new AnimationController<GeoAnimatable>(this, "fire", 10, (state) -> PlayState.CONTINUE)
                .triggerableAnim("shoot", SHOOT_ANIM));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    private <E extends GeoAnimatable> PlayState defaultPredicate(AnimationState<E> state) {
        if (state.isMoving()) {
            state.getController().setAnimation(WALK);
        } else {
            state.getController().setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        Weapon weapon = randomWeapon();
        ItemStack gun = weapon.getWeapon();
        if (gun == null) return null;
        this.setItemInHand(InteractionHand.MAIN_HAND, gun);
        finalizeInvs(weapon);
        finalizeArmors();
        operator.initialData();
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    private void finalizeInvs(Weapon weapon) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack ammo = weapon.getAmmo();
            if (ammo == null) return;
            inventory.addItem(ammo);
        }
    }

    private void finalizeArmors() {
        Item armor = ForgeRegistries.ITEMS.getValue(new ResourceLocation("zerocontact", "armor_jpc_v1"));
        if (armor != null) {
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(armor));
            CuriosApi.getCuriosInventory(this).ifPresent(handler -> {
                Item plate = ForgeRegistries.ITEMS.getValue(new ResourceLocation("zerocontact", "plate_steel"));
                if (plate != null) {
                    ItemStack plateStack = new ItemStack(plate);
                    handler.setEquippedCurio("front_plate", 0, plateStack);
                    handler.setEquippedCurio("back_plate", 0, plateStack.copy());
                }
            });
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
    }

    private @NotNull NonNullList<ItemStack> randomAddLootsList() {
        NonNullList<ItemStack> stackList = NonNullList.create();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (random.nextFloat() <= .07F && !inventory.getItem(i).isEmpty()) {
                stackList.add(inventory.getItem(i).copy());
            }
        }
        return stackList;
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

    private void listenPlayVoice() {
        GlobalStateController.VoiceManager manager = stateController.voiceManager;
        manager.playSound(GlobalStateController.Voice.CONTACT,this,()->stateController.getPhase() == GlobalStateController.Phase.ATTACK);
        manager.playSound(GlobalStateController.Voice.RELOAD,this,()->operator.getSynReloadState().getStateType().isReloading());
        manager.playSound(GlobalStateController.Voice.HURT,this,()->stateController.getShareContext().isHurt);
    }

    @Override
    public void tick() {
        super.tick();
        listenPlayVoice();
        attackPredicate();
        stateController.tick();
    }

    private void attackPredicate() {
        if (this.level().isClientSide) return;
        if (stateController.getPhase() == GlobalStateController.Phase.ATTACK && !this.walkAnimation.isMoving()) {
            this.triggerAnim("fire", "shoot");
        } else {
            this.stopTriggeredAnimation("fire", "shoot");
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        stateController.onHurt();
        return super.hurt(source, amount);
    }


    @Override
    public @NotNull SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        GroundPathNavigation groundPathNavigation = new GroundPathNavigation(this, level);
        groundPathNavigation.setCanPassDoors(true);
        groundPathNavigation.setCanOpenDoors(true);
        return groundPathNavigation;
    }
}
