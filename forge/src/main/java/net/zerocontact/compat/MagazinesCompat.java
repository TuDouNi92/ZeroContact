package net.zerocontact.compat;

import com.raiiiden.taczmagazines.capability.GunMagazineCapability;
import com.raiiiden.taczmagazines.capability.GunMagazineProvider;
import com.raiiiden.taczmagazines.config.MechanicsConfig;
import com.raiiiden.taczmagazines.item.AmmoBoxMagazineStorage;
import com.raiiiden.taczmagazines.item.MagazineItem;
import com.raiiiden.taczmagazines.item.MagazineRegistrar;
import com.raiiiden.taczmagazines.item.MagazineReloadSource;
import com.raiiiden.taczmagazines.magazine.MagazineFamilySystem;
import com.raiiiden.taczmagazines.magazine.SplitReloadGuns;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.caliber.ICartridgeHolder;
import net.zerocontact.caliber.AmmoInjector;
import net.zerocontact.caliber.CaliberSerializer;
import net.zerocontact.compat.magazines.MagazineRoundStack;
import net.zerocontact.compat.magazines.MagazineRoundStack.Round;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.item.ammo.GenerateAmmo;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.raiiiden.taczmagazines.item.MagazineAmmoSource.*;

public class MagazinesCompat {
    private static final String CHAMBER = "ZeroContact_ChamberRound";
    private static final String FEED = "ZeroContact_FeedRound";
    private final ThreadLocal<Transfer> transfer = new ThreadLocal<>();

    private static boolean isEmpty(@Nullable ItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    private @Nullable GunData gunData(ItemStack gunStack) {
        IGun gun = IGun.getIGunOrNull(gunStack);
        ResourceLocation id = gun == null ? null : gun.getGunId(gunStack);
        return id == null ? null : TimelessAPI.getCommonGunIndex(id).map(CommonGunIndex::getGunData).orElse(null);
    }

    private @Nullable ResourceLocation gunAmmoId(ItemStack gunStack) {
        GunData data = gunData(gunStack);
        return data == null ? null : data.getAmmoId();
    }

    /**
     * Detachable magazine guns, including their retained chamber after removing the magazine.
     */
    public boolean managesGun(ItemStack gunStack) {
        return managedGun(gunStack) != null;
    }

    private @Nullable IGun managedGun(ItemStack gunStack) {
        if (isEmpty(gunStack)) return null;
        IGun gun = IGun.getIGunOrNull(gunStack);
        return gun != null && isMagazineCompatibleWithGun(gunStack)
                && !gun.useInventoryAmmo(gunStack) && !gun.useDummyAmmo(gunStack)
                && !SplitReloadGuns.isSplitReload(gunStack)
                && (getMag(gunStack).getItem() instanceof MagazineItem || gunRound(gunStack, CHAMBER) != null)
                ? gun : null;
    }

    private @Nullable Round roundFrom(ItemStack stack, @Nullable ResourceLocation fallback) {
        if (isEmpty(stack)) return null;
        // Cartridge identity only needs NBT; resolving full gun ballistics can require missing assets.
        Round round = gunRound(stack, CaliberSerializer.AI_AMMO);
        if (round != null) return round;
        return fallback == null || fallback.equals(DefaultAssets.EMPTY_AMMO_ID) ? null
                : new Round(fallback.toString(), CaliberSerializer.DEFAULT_AMMO);
    }

    private @Nullable ResourceLocation magazineAmmoId(ItemStack magazine) {
        if (isEmpty(magazine) || !(magazine.getItem() instanceof MagazineItem)) return null;
        String family = MagazineItem.getMagazineFamilyId(magazine);
        return family == null || family.isBlank() ? null : MagazineFamilySystem.getAmmoTypeForFamily(family);
    }

    private @Nullable Round compatibleRound(ItemStack magazine, ItemStack ammo) {
        ResourceLocation required = magazineAmmoId(magazine);
        if (required == null || required.equals(DefaultAssets.EMPTY_AMMO_ID) || isEmpty(ammo)
                || !required.equals(compatibleAmmoId(ammo, required))) return null;
        // Universal creative boxes supply the requested caliber, regardless of their stored context.
        if (ammo.getItem() instanceof IAmmoBox box && box.isAllTypeCreative(ammo)) {
            return new Round(required.toString(), CaliberSerializer.DEFAULT_AMMO);
        }
        Round round = roundFrom(ammo, required);
        return round != null && required.toString().equals(round.ammoId()) ? round : null;
    }

    /**
     * Match the family's caliber while allowing different variants within that caliber.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canLoadAmmo(ItemStack magazine, ItemStack ammo) {
        return compatibleRound(magazine, ammo) != null;
    }

    private @Nullable Round legacyRound(ItemStack magazine) {
        if (isEmpty(magazine) || !(magazine.getItem() instanceof MagazineItem item)) return null;
        ResourceLocation id = magazineAmmoId(magazine);
        if (id == null) id = item.getAmmoId(magazine);
        return roundFrom(magazine, id);
    }

    private @Nullable MagazineRoundStack readRounds(ItemStack magazine, int count) {
        if (isEmpty(magazine) || !(magazine.getItem() instanceof MagazineItem)) return null;
        int capacity = Math.max(0, MagazineItem.getMaxCapacity(magazine));
        return MagazineRoundStack.load(magazine.getTag(), Math.min(Math.max(0, count), capacity), legacyRound(magazine));
    }

    private @Nullable Round topRound(ItemStack magazine, int count) {
        MagazineRoundStack rounds = readRounds(magazine, count);
        return rounds == null ? null : rounds.peek();
    }

    public void initializeMagazine(ItemStack magazine) {
        if (isEmpty(magazine) || !(magazine.getItem() instanceof MagazineItem item)) return;
        MagazineRoundStack rounds = readRounds(magazine, item.getAmmoCount(magazine));
        if (rounds != null) saveRounds(magazine, rounds);
    }

    private void saveRounds(ItemStack magazine, MagazineRoundStack rounds) {
        CompoundTag tag = magazine.getOrCreateTag();
        tag.put(MagazineRoundStack.TAG, rounds.save());
        tag.putInt("AmmoCount", rounds.size());
        Round top = rounds.peek();
        ((MagazineItem) magazine.getItem()).setAmmoId(magazine,
                top == null ? DefaultAssets.EMPTY_AMMO_ID : new ResourceLocation(top.ammoId()));
        if (top == null) {
            magazine.removeTagKey(CaliberSerializer.AI_AMMO);
        } else {
            tag.put(CaliberSerializer.AI_AMMO, top.save());
        }
    }

    /**
     * All item/packet count changes share this entry; copied magazine stacks work as well.
     */
    public void updateMagazineCount(ItemStack magazine, int previousCount, int newCount) {
        MagazineRoundStack rounds = readRounds(magazine, previousCount);
        if (rounds == null) {
            // setAmmoCount has already run; preserve the previous count when recovery is impossible.
            if (!isEmpty(magazine) && magazine.getItem() instanceof MagazineItem) {
                magazine.getOrCreateTag().putInt("AmmoCount", Math.max(0, previousCount));
            }
            return;
        }
        newCount = Math.min(Math.max(0, newCount), Math.max(0, MagazineItem.getMaxCapacity(magazine)));
        Transfer active = transfer.get();
        Round loaded = active != null && active.loaded != null ? active.loaded : legacyRound(magazine);
        ResourceLocation required = magazineAmmoId(magazine);
        // An invalid explicit source must not turn into a synthesized legacy cartridge.
        if (newCount > rounds.size() && (required == null || required.equals(DefaultAssets.EMPTY_AMMO_ID)
                || loaded == null || !required.toString().equals(loaded.ammoId())
                || (active != null && active.hasAmmoSource && active.loaded == null))) {
            newCount = rounds.size();
        }
        while (rounds.size() > newCount) rounds.pop();
        while (rounds.size() < newCount) rounds.push(loaded);
        saveRounds(magazine, rounds);
    }

    /**
     * Scoped source/snapshot, never kept in Item singleton fields or persisted as transient NBT.
     */
    public final class Transfer implements AutoCloseable {
        private final @Nullable Transfer previous;
        private final MagazineRoundStack returned;
        private final @Nullable Player player;
        private boolean hasAmmoSource;
        private @Nullable Round loaded;

        private Transfer(ItemStack magazine, ItemStack ammo, @Nullable Player player) {
            previous = transfer.get();
            this.player = player;
            hasAmmoSource = !isEmpty(ammo);
            if (!isEmpty(magazine) && magazine.getItem() instanceof MagazineItem item) {
                initializeMagazine(magazine);
                MagazineRoundStack rounds = readRounds(magazine, item.getAmmoCount(magazine));
                returned = rounds == null ? new MagazineRoundStack() : rounds.copy();
                loaded = compatibleRound(magazine, ammo);
            } else {
                returned = new MagazineRoundStack();
            }
            transfer.set(this);
        }

        @Override
        public void close() {
            if (previous == null) transfer.remove();
            else transfer.set(previous);
        }
    }

    public Transfer beginTransfer(ItemStack magazine, ItemStack ammo, Player player) {
        return new Transfer(magazine, ammo, player);
    }

    public void recordLoadedRound(ItemStack magazine, ItemStack ammo) {
        Transfer active = transfer.get();
        if (active != null) {
            active.hasAmmoSource = true;
            active.loaded = compatibleRound(magazine, ammo);
        }
    }

    public boolean takeRoundFromInventory(ItemStack magazine, Player player, ResourceLocation requiredAmmo) {
        if (player == null) return false;
        ItemStack source = MechanicsConfig.PREFER_PLAYER_INVENTORY.get()
                ? takeLoose(magazine, player, requiredAmmo) : takeFromBox(magazine, player, requiredAmmo);
        if (source.isEmpty()) return false;
        recordLoadedRound(magazine, source);
        return true;
    }

    private ItemStack createRound(@Nullable Round round) {
        if (round == null) return ItemStack.EMPTY;
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(round.variant()));
        return item instanceof GenerateAmmo ? item.getDefaultInstance()
                : AmmoItemBuilder.create().setId(new ResourceLocation(round.ammoId())).setCount(1).build();
    }

    /**
     * Replace a vanilla unload chunk with its real variants, including bulk unloads.
     */
    public ItemStack returnedRounds(ItemStack original) {
        if (isEmpty(original)) return ItemStack.EMPTY;
        Transfer active = transfer.get();
        if (active == null || active.player == null || active.returned.size() == 0) return original;
        ItemStack first = ItemStack.EMPTY;
        ItemStack group = ItemStack.EMPTY;
        for (int i = 0; i < original.getCount() && active.returned.size() > 0; i++) {
            @Nullable Round popped = active.returned.pop();
            ItemStack round = createRound(popped);
            if (!group.isEmpty() && ItemStack.isSameItemSameTags(group, round)
                    && group.getCount() < group.getMaxStackSize()) {
                group.grow(1);
            } else {
                if (!group.isEmpty()) {
                    if (first.isEmpty()) first = group;
                    else giveToPlayer(active.player, group);
                }
                group = round;
            }
        }
        if (first.isEmpty()) return group;
        if (!group.isEmpty()) giveToPlayer(active.player, group);
        return first;
    }

    private void giveToPlayer(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private @Nullable Round gunRound(ItemStack gun, String key) {
        return isEmpty(gun) || gun.getTag() == null ? null : Round.load(gun.getTag().getCompound(key));
    }

    private void applyRound(ItemStack gun, @Nullable Round round) {
        if (isEmpty(gun) || round == null) return;
        CompoundTag root = new CompoundTag();
        root.put(CaliberSerializer.AI_AMMO, round.save());
        AmmoInjector.AmmoContext context = CaliberSerializer.load(root, gun);
        if (context.isEmpty()) return;
        AmmoInjector.write(context, gun);
        gun.getOrCreateTagElement(CaliberSerializer.AI_AMMO)
                .putString(CaliberSerializer.EXISTED_VARIANT, round.variant());
    }

    public void captureChamber(ItemStack gunStack) {
        IGun gun = managedGun(gunStack);
        if (gun == null) return;
        if (gun.hasBulletInBarrel(gunStack) && gunRound(gunStack, CHAMBER) == null) {
            Round round = roundFrom(gunStack, gunAmmoId(gunStack));
            if (round != null) gunStack.getOrCreateTag().put(CHAMBER, round.save());
        }
    }

    public void beforeStoreMagazine(ItemStack gunStack, ItemStack magazine) {
        captureChamber(gunStack);
        initializeMagazine(magazine);
    }

    private @Nullable Round popFromGun(ItemStack gunStack, int previousCount, int amount) {
        ItemStack magazine = getMag(gunStack);
        if (!(magazine.getItem() instanceof MagazineItem)) return null;
        MagazineRoundStack rounds = readRounds(magazine, previousCount);
        if (rounds == null) return null;
        Round result = null;
        for (int i = 0; i < amount && rounds.size() > 0; i++) result = rounds.pop();
        saveRounds(magazine, rounds);
        gunStack.getCapability(GunMagazineProvider.GUN_MAGAZINE).ifPresent(cap -> cap.setStoredMagazine(magazine));
        return result;
    }

    /**
     * Called once for each delayed burst callback, before its event/consume/spawn sequence.
     */
    public int beginShot(ItemStack gunStack, int originalBulletAmount) {
        IGun gun = managedGun(gunStack);
        if (gun == null) return originalBulletAmount;
        captureChamber(gunStack);
        Round current = gun.hasBulletInBarrel(gunStack) ? gunRound(gunStack, CHAMBER) : null;
        ItemStack magazine = getMag(gunStack);
        if (current == null && magazine.getItem() instanceof MagazineItem) {
            current = topRound(magazine, gun.getCurrentAmmoCount(gunStack));
        }
        applyRound(gunStack, current);
        if (current == null) return originalBulletAmount;
        AmmoInjector.AmmoContext context = AmmoInjector.read(gunStack);
        return context.isEmpty() ? originalBulletAmount : Math.max(1, context.caliber().bulletAmount());
    }

    /**
     * Commit only successful consumption; keep the firing context until all pellets have spawned.
     */
    public void consumedShot(ItemStack gunStack, int previousCount, boolean hadChamber) {
        IGun gun = managedGun(gunStack);
        if (gun == null) return;
        Round fired = hadChamber ? gunRound(gunStack, CHAMBER) : null;
        int removed = previousCount - gun.getCurrentAmmoCount(gunStack);
        if (removed > 0) {
            Round round = popFromGun(gunStack, previousCount, removed);
            if (fired == null) fired = round;
            if (hadChamber && gun.hasBulletInBarrel(gunStack) && round != null) {
                gunStack.getOrCreateTag().put(CHAMBER, round.save());
            }
        }
        applyRound(gunStack, fired);
        if (!gun.hasBulletInBarrel(gunStack)) gunStack.removeTagKey(CHAMBER);
    }

    public void removedFromMagazine(ItemStack gunStack, int previousCount, int removed) {
        if (!managesGun(gunStack) || removed <= 0) return;
        Round round = popFromGun(gunStack, previousCount, removed);
        if (round != null) gunStack.getOrCreateTag().put(FEED, round.save());
    }

    public void setChamber(ItemStack gunStack, boolean chambered) {
        IGun gun = managedGun(gunStack);
        if (gun == null) return;
        if (chambered) {
            Round round = gunRound(gunStack, FEED);
            ItemStack magazine = getMag(gunStack);
            // Some scripts account for the chamber by inserting N-1, without a removeAmmoFromMagazine call.
            if (round == null && magazine.getItem() instanceof MagazineItem item) {
                int count = item.getAmmoCount(magazine);
                int unaccounted = count - gun.getCurrentAmmoCount(gunStack);
                if (unaccounted > 0) round = popFromGun(gunStack, count, unaccounted);
            }
            if (round == null) round = gunRound(gunStack, CHAMBER);
            if (round == null) round = roundFrom(gunStack, gunAmmoId(gunStack));
            if (round != null) gunStack.getOrCreateTag().put(CHAMBER, round.save());
            applyRound(gunStack, round);
        } else {
            gunStack.removeTagKey(CHAMBER);
        }
        gunStack.removeTagKey(FEED);
    }

    public void endShot(ItemStack gunStack) {
        IGun gun = managedGun(gunStack);
        if (gun == null) return;
        Round next = gun.hasBulletInBarrel(gunStack) ? gunRound(gunStack, CHAMBER) : null;
        ItemStack magazine = getMag(gunStack);
        if (next == null && magazine.getItem() instanceof MagazineItem) {
            next = topRound(magazine, gun.getCurrentAmmoCount(gunStack));
        }
        applyRound(gunStack, next);
    }

    public boolean instanceOfMagazine(Item object) {
        return object instanceof MagazineItem;
    }

    public boolean hasUsableMagazine(IItemHandler handler, ItemStack gun) {
        return handler != null && !isEmpty(gun) && IGun.getIGunOrNull(gun) != null
                && MagazineReloadSource.hasUsableMagazine(handler, gun);
    }

    public boolean hasCompatibleMagazineInBox(ItemStack box, ItemStack gun) {
        return !isEmpty(box) && !isEmpty(gun) && IGun.getIGunOrNull(gun) != null
                && !isEmpty(AmmoBoxMagazineStorage.peekBestCompatible(box, gun));
    }

    public ItemStack getCompatibleMag(ItemStack gunStack) {
        if (isEmpty(gunStack)) return ItemStack.EMPTY;
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) return ItemStack.EMPTY;
        ResourceLocation gunId = gun.getGunId(gunStack);
        String familyId = MagazineFamilySystem.getFamilyForGun(gunId);
        if (familyId == null || familyId.isBlank()) return ItemStack.EMPTY;
        GunData gunData = gunData(gunStack);
        if (gunData == null || gunData.getAmmoId() == null || !MagazineRegistrar.MAGAZINE.isPresent()) {
            return ItemStack.EMPTY;
        }
        List<String> extFamilies = MagazineFamilySystem.getExtendedFamiliesForBaseFamily(familyId);
        int extLevel = AttachmentDataUtils.getMagExtendLevel(gunStack, gunData);
        String familyIdWithExt = extFamilies.stream().filter(s -> MagazineFamilySystem.getExtLevelForFamily(s) == extLevel).findAny().orElse("");
        return gunStack.getCapability(GunMagazineProvider.GUN_MAGAZINE).map(cap -> {
            Item magItem = MagazineRegistrar.MAGAZINE.get();
            String id = familyId;
            if (!familyIdWithExt.isEmpty()) {
                id = familyIdWithExt;
            }
            return MagazineItem.createMagazineByFamily(magItem, id, MagazineFamilySystem.getCapacityForFamily(id), gunData.getAmmoId());

        }).orElse(ItemStack.EMPTY);

    }

    public boolean isMagazineCompatibleWithGun(ItemStack gunStack) {
        if (isEmpty(gunStack)) return false;
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) return false;
        ResourceLocation gunId = gun.getGunId(gunStack);
        String familyId = MagazineFamilySystem.getFamilyForGun(gunId);
        return familyId != null && !familyId.isEmpty();
    }

    public ItemStack getMag(ItemStack gunStack) {
        if (isEmpty(gunStack)) return ItemStack.EMPTY;
        return gunStack.getCapability(GunMagazineProvider.GUN_MAGAZINE).map(GunMagazineCapability::getStoredMagazine).orElse(ItemStack.EMPTY);
    }

    public void setVariantFromMag(ItemStack gunStack, ItemStack magStack, ICartridgeHolder cap) {
        if (isEmpty(gunStack) || IGun.getIGunOrNull(gunStack) == null) return;
        IGun gun = managedGun(gunStack);
        if (gun != null) {
            initializeMagazine(magStack);
            Round chamber = gun.hasBulletInBarrel(gunStack) ? gunRound(gunStack, CHAMBER) : null;
            applyRound(gunStack, chamber == null ? topRound(magStack, getAmmoCount(magStack)) : chamber);
            return;
        }
        if (!isEmpty(magStack) && magStack.getItem() instanceof MagazineItem magazineItem) {
            if (magazineItem.isAmmoBoxOfGun(gunStack, magStack)) {
                AmmoInjector.AmmoContext context = AmmoInjector.read(magStack);
                if (context.isEmpty()) {
                    ResourceLocation defaultAmmo = gunAmmoId(gunStack);
                    if (defaultAmmo != null)
                        applyRound(gunStack, new Round(defaultAmmo.toString(), CaliberSerializer.DEFAULT_AMMO));
                } else if (cap != null) {
                    cap.setAmmoVariantInGun(gunStack, context.caliber().variant());
                }
            }
        }
    }

    public ItemStack prepareCreativeMagazine(IItemHandler inventory, ItemStack gun, int selectedSlot, ItemStack original) {
        ItemStack fallback = isEmpty(original) ? ItemStack.EMPTY : original;
        if (isEmpty(gun) || IGun.getIGunOrNull(gun) == null
                || (selectedSlot >= 0 && (inventory == null || selectedSlot >= inventory.getSlots()))) {
            return fallback;
        }
        ItemStack magazine = selectedSlot >= 0 ? original : getCompatibleMag(gun);
        if (isEmpty(magazine) || !(magazine.getItem() instanceof MagazineItem)) return fallback;
        ItemStack source = selectedSlot >= 0
                ? inventory.getStackInSlot(selectedSlot) : gun;
        Round selectedRound = roundFrom(source, magazineAmmoId(magazine));
        if (magazine.getTag() == null || !magazine.getTag().contains(MagazineRoundStack.TAG)) {
            if (selectedRound != null) magazine.getOrCreateTag().put(CaliberSerializer.AI_AMMO, selectedRound.save());
        }
        // Selected mixed magazines retain their order; the native refill extends them with their top round.
        initializeMagazine(magazine);
        gun.getCapability(CapabilityRegistries.CARTRIDGE).ifPresent(cap -> {
            if (selectedSlot >= 0 && selectedRound != null) {
                cap.setClientSelectedAmmoVariant(gun, selectedRound.variant());
            }
            Round chamber = gunRound(gun, CHAMBER);
            applyRound(gun, chamber == null ? topRound(magazine, getAmmoCount(magazine)) : chamber);
        });
        return magazine;
    }

    public ItemStack takeLoose(ItemStack mag, Player player, ResourceLocation requiredAmmo) {
        if (player == null || requiredAmmo == null || !requiredAmmo.equals(magazineAmmoId(mag))) return ItemStack.EMPTY;
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof IAmmo)
                    || !canLoadAmmo(mag, stack)) {
                continue;
            }
            ItemStack result = stack.copy();
            result.setCount(1);
            stack.shrink(1);
            player.getInventory().setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    public ItemStack takeFromBox(ItemStack mag, Player player, ResourceLocation requiredAmmo) {
        if (player == null || requiredAmmo == null || !requiredAmmo.equals(magazineAmmoId(mag))) return ItemStack.EMPTY;
        ItemStack result = ItemStack.EMPTY;
        if (MechanicsConfig.LOAD_MAGAZINES_FROM_AMMO_BOXES.get()) {
            for (ItemStack stack : player.getInventory().items) {
                if (!AmmoBoxMagazineStorage.isExternalAmmoBox(stack) || !canLoadAmmo(mag, stack) || available(stack) <= 0) {
                    continue;
                }
                result = stack.copy();
                consume(stack, 1);
                player.getInventory().setChanged();
                return result;
            }
        }
        return result;
    }

    public boolean isAmmoBoxOfGun(ItemStack stack, ItemStack slot) {
        if (!isEmpty(stack) && !isEmpty(slot) && IGun.getIGunOrNull(stack) != null
                && slot.getItem() instanceof MagazineItem magazineItem) {
            return magazineItem.isAmmoBoxOfGun(stack, slot);
        }
        return false;
    }

    public int getAmmoCount(ItemStack slot) {
        if (!isEmpty(slot) && slot.getItem() instanceof MagazineItem magazineItem) {
            return magazineItem.getAmmoCount(slot);
        }
        return 0;
    }
}
