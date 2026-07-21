package net.zerocontact.entity;

import com.tacz.guns.api.entity.IGunOperator;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.zerocontact.api.IEquipmentTypeTag;
import net.zerocontact.entity.ai.NameList;
import net.zerocontact.entity.ai.controller.GlobalStateController;
import net.zerocontact.entity.ai.goal.AvoidGoal;
import net.zerocontact.entity.ai.goal.LongRangeAttackableTargetGoal;
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

import java.util.List;
import java.util.Optional;

public class ArmedRaider extends PatrollingMonster implements GeoEntity, InventoryCarrier {
    public static final String FRONT_PLATE = "front_plate";
    public static final String BACK_PLATE = "back_plate";
    public static final String BACKPACK = "backpack";
    public static final String ARMBAND = "armband";

    public static final String GUN_ID = "gun_id";
    public static final String CARTRIDGE_ID = "cartridge_id";
    public static final String HELMET_ID = "helmet_id";
    public static final String ARMOR_ID = "armor_id";
    public static final String PLATE_ID = "plate_id";
    public static final String BACKPACK_ID = "backpack_id";
    public static final String ARMBAND_ID = "armband_id";

    public static final String INITIALIZED = "initialized";
    public static final String TACZ_NAMESPACE_PREFIX = "tacz:";
    public static final String TACZ_TAG = "tacz";
    private final AnimatableInstanceCache geoCache;
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().then("raider.animation.alert", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation WALK = RawAnimation.begin().then("raider.animation.walk", Animation.LoopType.LOOP);
    private static final RawAnimation IDLE = RawAnimation.begin().then("raider.animation.idle", Animation.LoopType.LOOP);
    public final RandomSource random = this.getRandom();
    private final SimpleContainer inventory = new SimpleContainer(7);
    private final LazyOptional<IItemHandler> itemHandlerLazyOptional = LazyOptional.of(() -> new InvWrapper(inventory));
    private final IGunOperator operator;
    public final GlobalStateController stateController;
    private LoadoutHolder.LoadoutData loadout = randomLoadout()
            .setArmor(null)
            .setPlate(null)
            .setHelmet(null)
            .setBackpack(null)
            .setArmband(null)
            .build();
    private boolean loadOutInitialized;

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
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new PerformGunAttackGoal(this));
        this.goalSelector.addGoal(5, RestrictedGoalWrapper.create(this, new MeleeAttackGoal(this, 1.2f, false), GlobalStateController.Phase.ATTACK));
        this.goalSelector.addGoal(5, RestrictedGoalWrapper.create(this, new RandomStrollGoal(this, 0.8F)));
        this.goalSelector.addGoal(8, RestrictedGoalWrapper.create(this, new RandomLookAroundGoal(this)));
        this.targetSelector.addGoal(2, new LongRangeAttackableTargetGoal<>(this, Player.class, true, false, 12.0D));
        this.targetSelector.addGoal(1, new LongRangeAttackableTargetGoal<>(this, Monster.class, true, false, 12.0D));
        this.targetSelector.addGoal(1, new LongRangeAttackableTargetGoal<>(this, IronGolem.class, true, false, 12.0D));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.MAX_HEALTH, 30.0F)
                .add(Attributes.ATTACK_DAMAGE, 1.0F)
                .add(Attributes.FOLLOW_RANGE, 75.0F);
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
        prepareLoadout();
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    private void prepareLoadout() {
        ItemStack gun = loadout.gunStack();
        if (gun == null) return;
        this.setItemInHand(InteractionHand.MAIN_HAND, gun);
        operator.initialData();
        if (loadOutInitialized) return;
        finalizeLoadout();
    }

    private void finalizeInvs() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) continue;
            ItemStack ammo = loadout.ammoStack();
            if (ammo == null) return;
            inventory.addItem(ammo);
        }
    }

    private void finalizeArmors() {
        Item armor = loadout.armorStack().getItem();
        Item helmet = loadout.helmetStack().getItem();
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(armor));
        if (armor instanceof IEquipmentTypeTag tag && tag.getArmorType().equals(IEquipmentTypeTag.EquipmentType.PLATE_CARRIER)) {
            CuriosApi.getCuriosInventory(this).ifPresent(handler -> {
                Item plate = loadout.plateStack().getItem();
                ItemStack plateStack = new ItemStack(plate);
                handler.setEquippedCurio(FRONT_PLATE, 0, plateStack);
                handler.setEquippedCurio(BACK_PLATE, 0, plateStack.copy());
            });
        }
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(helmet));
    }

    private void finalizeGears() {
        Item backpack = loadout.backpackStack().getItem();
        Item armband = loadout.armbandStack().getItem();
        CuriosApi.getCuriosInventory(this).ifPresent(handler -> {
            if (backpack instanceof IEquipmentTypeTag tag && tag.getArmorType().equals(IEquipmentTypeTag.EquipmentType.BACKPACK)) {
                handler.setEquippedCurio(BACKPACK, 0, new ItemStack(backpack));
            }
            handler.setEquippedCurio(ARMBAND, 0, new ItemStack(armband));
        });
    }

    private void finalizeLoadout() {
        finalizeInvs();
        finalizeArmors();
        finalizeGears();
        loadOutInitialized = true;
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource damageSource, int looting, boolean hitByPlayer) {
        ItemStack mainHandItem = getMainHandItem();
        if (mainHandItem.isEmpty()) return;
        if (random.nextFloat() < .3F) {
            this.spawnAtLocation(mainHandItem.copy());
        }
        Containers.dropContents(this.level(), this.getOnPos(), getRandomLootList());
    }

    private @NotNull NonNullList<ItemStack> getRandomLootList() {
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
        CompoundTag taczTag = createWeaponTag();
        compound.put(TACZ_TAG, taczTag);
        writeInventoryToTag(compound);
        super.addAdditionalSaveData(compound);
    }

    private @NotNull CompoundTag createWeaponTag() {
        CompoundTag taczTag = new CompoundTag();
        taczTag.putString(GUN_ID, loadout.gunId());
        taczTag.putString(CARTRIDGE_ID, Optional.ofNullable(loadout.ammoId()).orElse(TACZ_NAMESPACE_PREFIX + loadout.ammoStack().getItem()));
        taczTag.putBoolean(INITIALIZED, loadOutInitialized);
        return taczTag;
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        readInventoryFromTag(compound);
        importWeaponTag(compound);
    }

    private void importWeaponTag(@Nullable CompoundTag dataTag) {
        if (dataTag != null) {
            CompoundTag taczTag = dataTag.getCompound(TACZ_TAG);
            String gunId = taczTag.getString(GUN_ID);
            String ammoId = taczTag.getString(CARTRIDGE_ID);
            String helmetId = taczTag.getString(HELMET_ID);
            String armorId = taczTag.getString(ARMOR_ID);
            String plateId = taczTag.getString(PLATE_ID);
            String backPackId = taczTag.getString(BACKPACK_ID);
            String armbandId = taczTag.getString(ARMBAND_ID);
            loadOutInitialized = taczTag.getBoolean(INITIALIZED);
            if (taczTag.isEmpty()) return;
            if (!ammoId.isEmpty()) {
                loadout = LoadoutHolder.create(gunId, ammoId)
                        .setHelmet(helmetId)
                        .setArmor(armorId)
                        .setPlate(plateId)
                        .setBackpack(backPackId)
                        .setArmband(armbandId)
                        .build();
                prepareLoadout();
            } else {
                loadout = LoadoutHolder.create(gunId, null)
                        .setHelmet(helmetId)
                        .setArmor(armorId)
                        .setPlate(plateId)
                        .setBackpack(backPackId)
                        .setArmband(armbandId)
                        .build();
                prepareLoadout();
            }
        }
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
        manager.playSound(GlobalStateController.Voice.CONTACT, this, () -> stateController.getPhase() == GlobalStateController.Phase.ATTACK);
        manager.playSound(GlobalStateController.Voice.RELOAD, this, () -> operator.getSynReloadState().getStateType().isReloading());
        manager.playSound(GlobalStateController.Voice.HURT, this, () -> stateController.getShareContext().isHurt);
    }

    @Override
    public void tick() {
        super.tick();
        listenPlayVoice();
        attackAnimPredicate();
        stateController.tick();
    }

    private void attackAnimPredicate() {
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

    @Override
    public int getMaxFallDistance() {
        return 6;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isAlliedTo(@NotNull Entity entity) {
        if (super.isAlliedTo(entity)) {
            return true;
        } else if (entity instanceof LivingEntity livingEntity && livingEntity.getClass() == this.getClass()) {
            return this.getTeam() == null && entity.getTeam() == null;
        }
        return false;
    }

    @Override
    public boolean canPickUpLoot() {
        Vec3i vec3i = this.getPickupReach();
        AABB searchArea = this.getBoundingBox().inflate(vec3i.getX(), vec3i.getY(), vec3i.getZ());
        List<? extends Entity> ammoItems = this.level().getEntitiesOfClass(ItemEntity.class, searchArea,
                entity -> ItemStack.isSameItemSameTags(entity.getItem(), loadout.ammoStack()));
        return !ammoItems.isEmpty() || super.canPickUpLoot();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.level().getProfiler().push("looting");
        Vec3i vec3i = this.getPickupReach();
        if (!this.level().isClientSide && this.canPickUpLoot() && this.isAlive() && !this.dead && ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
            for (ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(vec3i.getX(), vec3i.getY(), vec3i.getZ()),
                    entity -> ItemStack.isSameItemSameTags(entity.getItem(), loadout.ammoStack()))) {
                if (!itementity.isRemoved() && !itementity.getItem().isEmpty() && !itementity.hasPickUpDelay()) {
                    this.pickUpItem(itementity);
                }
            }
        }

        this.level().getProfiler().pop();
    }

    @Override
    protected void pickUpItem(@NotNull ItemEntity itemEntity) {
        super.pickUpItem(itemEntity);
        ItemStack pickableStack = itemEntity.getItem();
        if (inventory.canAddItem(pickableStack)) {
            ItemStack addedStack = inventory.addItem(pickableStack);
            if (addedStack.isEmpty()) itemEntity.discard();
        }
    }

    private LoadoutHolder randomLoadout() {
        LoadoutHolder.LoadoutTypes[] values = LoadoutHolder.LoadoutTypes.values();
        return values[random.nextInt(values.length)].loadoutHolder.get();
    }
}
