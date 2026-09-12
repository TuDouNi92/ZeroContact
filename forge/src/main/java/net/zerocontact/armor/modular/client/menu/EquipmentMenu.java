package net.zerocontact.armor.modular.client.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import net.zerocontact.api.armor.modular.ModularEquipment;
import net.zerocontact.armor.modular.client.network.ItemStackCodec;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.forge_registries.MenuRegistry;
import net.zerocontact.network.ModMessages;
import net.zerocontact.armor.modular.client.network.c2s.SelectEquipmentMountPacket;
import net.zerocontact.armor.modular.client.network.c2s.MountPacket;
import net.zerocontact.armor.modular.client.network.c2s.UnMountPacket;
import net.zerocontact.armor.modular.service.ModuleMountService;
import net.zerocontact.armor.modular.service.ModuleSyncService;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.armor.modular.client.network.s2c.SyncEquipmentCandidatesPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Server-authoritative candidates, refreshed as the selected mount or inventory changes.
 */
public class EquipmentMenu extends AbstractContainerMenu {
    public static final int CREATIVE_SLOT = -1;
    public static final ResourceLocation NO_MOUNT = new ResourceLocation("zerocontact", "none");
    private final List<ItemStack> displayStacks = new ArrayList<>();
    public final List<ItemStack> renderStacks = Collections.unmodifiableList(displayStacks);
    private final Inventory playerInv;
    private ItemStack equipment;
    private EquipmentTarget target;
    private ResourceLocation mountId;
    private final List<Candidate> candidates = new ArrayList<>();
    private int candidateRevision;

    /**
     * Inventory slot is -1 for creative catalogue entries; stack is a display copy.
     */
    public record Candidate(int inventorySlot, ItemStack stack) {
        public Candidate {
            stack = stack.copy();
        }
    }

    //Client
    public EquipmentMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readItem(), buf.readResourceLocation(),
                EquipmentTarget.read(buf),
                buf.readList(buffer -> new Candidate(buffer.readVarInt(), ItemStackCodec.read(buf))));
    }

    private EquipmentMenu(int containerId, Inventory playerInv, ItemStack equipment,
                          ResourceLocation mountId, EquipmentTarget target, List<Candidate> candidates) {
        super(MenuRegistry.EQUIPMENT_MENU.get(), containerId);
        this.playerInv = playerInv;
        this.equipment = equipment;
        this.target = target;
        replaceCandidates(mountId, candidates);
    }

    public static List<Candidate> collectCandidates(Inventory inventory, ItemStack equipment,
                                                    ResourceLocation mountId) {
        if (equipment.isEmpty() || !(equipment.getItem() instanceof ModularEquipment modular)
                || inventory.player.isSpectator()) {
            return List.of();
        }
        var mount = modular.getMountDefinition(equipment, mountId).orElse(null);
        if (mount == null) return List.of();

        if (inventory.player.isCreative()) {
            return ModuleRegistry.getModulesFor(mount).stream()
                    .map(stack -> new Candidate(CREATIVE_SLOT, stack))
                    .toList();
        }

        List<Candidate> result = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || stack == equipment) continue;
            MountCategory category = ModuleRegistry.getCategory(stack);
            if (category != MountCategory.UNDEFINED && mount.acceptsModule(category)) {
                result.add(new Candidate(slot, stack));
            }
        }
        return List.copyOf(result);
    }

    /**
     * Server only, with a validated equipment reference.
     */
    public static void open(ServerPlayer player, ItemStack equipment, ResourceLocation mountId, Component title) {
        if (!player.isAlive() || player.isSpectator() || equipment.isEmpty()
                || !(equipment.getItem() instanceof ModularEquipment modular)
                || modular.getMountDefinition(equipment, mountId).isEmpty()) {
            return;
        }
        openMenu(player, equipment, mountId, title);
    }

    /**
     * Opens the player preview with no selected anchor and an empty module list.
     */
    public static void open(ServerPlayer player, Component title) {
        if (player.isAlive() && !player.isSpectator()) openMenu(player, ItemStack.EMPTY, NO_MOUNT, title);
    }

    private static EquipmentTarget findTarget(Player player, ItemStack equipment) {
        if (equipment.isEmpty()) return EquipmentTarget.NONE;
        return EquipmentTarget.wornBy(player).entrySet().stream()
                .filter(entry -> entry.getValue() == equipment).map(java.util.Map.Entry::getKey)
                .findFirst().orElse(EquipmentTarget.NONE);
    }

    private static void openMenu(ServerPlayer player, ItemStack equipment, ResourceLocation mountId, Component title) {
        EquipmentTarget target = findTarget(player, equipment);
        List<Candidate> candidates = collectCandidates(player.getInventory(), equipment, mountId);
        NetworkHooks.openScreen(player,
                new SimpleMenuProvider((id, inv, owner) ->
                        new EquipmentMenu(id, inv, equipment, mountId, target, candidates), title),
                buf -> {
                    buf.writeItem(equipment);
                    buf.writeResourceLocation(mountId);
                    target.write(buf);
                    buf.writeCollection(candidates, (buffer, candidate) -> {
                        buffer.writeVarInt(candidate.inventorySlot());
                        ItemStackCodec.write(buf, candidate.stack);
                    });
                });
    }

    public ResourceLocation getMountId() {
        return mountId;
    }

    public EquipmentTarget getTarget() {
        return target;
    }

    public boolean hasSelection() {
        return !equipment.isEmpty() && !mountId.equals(NO_MOUNT);
    }

    public List<Candidate> getCandidates() {
        return Collections.unmodifiableList(candidates);
    }

    public int getCandidateRevision() {
        return candidateRevision;
    }

    /**
     * Select a current card and request installation on the selected anchor.
     */
    public boolean mountCandidate(Candidate candidate) {
        if (!playerInv.player.level().isClientSide || !hasSelection()
                || !candidates.contains(candidate)) return false;
        ModMessages.sendToServer(new MountPacket(containerId, target, mountId,
                candidate.inventorySlot(), candidate.stack()));
        return true;
    }

    public boolean unMount() {
        if (!playerInv.player.level().isClientSide || !hasSelection()) return false;
        ModMessages.sendToServer(new UnMountPacket(containerId, target, mountId));
        return true;
    }

    /**
     * Server only: remove the module from the currently selected, validated anchor.
     */
    public void unMount(EquipmentTarget requestedTarget, ResourceLocation requestedMount) {
        if (!(playerInv.player instanceof ServerPlayer player) || !stillValid(player)
                || !hasSelection() || !target.equals(requestedTarget) || !mountId.equals(requestedMount)
                || (!target.equals(EquipmentTarget.NONE) && target.resolve(player) != equipment)
                || !(equipment.getItem() instanceof ModularEquipment modular)
                || modular.getMountDefinition(equipment, mountId).isEmpty()) return;
        ItemStack removed = ModuleMountService.unMount(equipment, mountId);
        if (!removed.isEmpty()) {
            if (!player.isCreative()) {
                if (!playerInv.add(removed)) player.drop(removed, false);
            }
            playerInv.setChanged();
        }
        ModuleSyncService.sync(player, target);
        replaceCandidates(mountId, collectCandidates(playerInv, equipment, mountId));
        syncCandidates();
        player.inventoryMenu.broadcastChanges();
    }

    /**
     * Server only: resolve the candidate again instead of trusting the display stack.
     */
    public void mountCandidate(EquipmentTarget requestedTarget, ResourceLocation requestedMount,
                               int inventorySlot, ItemStack expectedStack) {
        if (!(playerInv.player instanceof ServerPlayer player) || !stillValid(player)
                || !hasSelection() || !target.equals(requestedTarget) || !mountId.equals(requestedMount)
                || (!target.equals(EquipmentTarget.NONE) && target.resolve(player) != equipment)) return;
        Candidate candidate = collectCandidates(playerInv, equipment, mountId).stream()
                .filter(entry -> entry.inventorySlot() == inventorySlot && ItemStack.matches(entry.stack(), expectedStack))
                .findFirst().orElse(null);
        if (candidate == null) return;
        var container = equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT).map(cap -> cap).orElse(null);
        if (container == null) return;
        ItemStack oldModule = container.getModule(mountId).copy();
        ItemStack module = candidate.stack().copy();
        module.setCount(1);
        if (!ModuleMountService.mount(equipment, mountId, module)) return;
        if (!player.isCreative()) {
            playerInv.getItem(inventorySlot).shrink(1);
            if (!oldModule.isEmpty() && !playerInv.add(oldModule)) player.drop(oldModule, false);
        }
        playerInv.setChanged();
        ModuleSyncService.sync(player, target);
        replaceCandidates(mountId, collectCandidates(playerInv, equipment, mountId));
        syncCandidates();
        player.inventoryMenu.broadcastChanges();
    }

    /**
     * May be called by the screen or on the server. The server validates the requested mount.
     */
    public void setMountId(ResourceLocation mountId) {
        selectAnchor(target, mountId);
    }

    public void selectAnchor(EquipmentTarget target, ResourceLocation mountId) {
        if (playerInv.player.level().isClientSide) {
            ModMessages.sendToServer(new SelectEquipmentMountPacket(containerId, target, mountId));
            return;
        }
        if (target.equals(EquipmentTarget.NONE) && !this.target.equals(EquipmentTarget.NONE)) return;
        ItemStack selectedEquipment = target.equals(EquipmentTarget.NONE) ? equipment : target.resolve(playerInv.player);
        if (!stillValid(playerInv.player)
                || selectedEquipment.isEmpty()
                || !(selectedEquipment.getItem() instanceof ModularEquipment modular)
                || modular.getMountDefinition(selectedEquipment, mountId).isEmpty()) return;
        if (!this.mountId.equals(mountId) || !this.target.equals(target) || equipment != selectedEquipment) {
            this.equipment = selectedEquipment;
            this.target = target;
            replaceCandidates(mountId, collectCandidates(playerInv, equipment, mountId));
            syncCandidates();
        }
    }

    private void replaceCandidates(ResourceLocation mountId, List<Candidate> updated) {
        List<Candidate> copy = updated.stream()
                .map(candidate -> new Candidate(candidate.inventorySlot(), candidate.stack())).toList();
        this.mountId = mountId;
        candidates.clear();
        candidates.addAll(copy);
        displayStacks.clear();
        copy.forEach(candidate -> displayStacks.add(candidate.stack().copy()));
        candidateRevision++;
    }

    public void applyCandidates(EquipmentTarget target, ItemStack equipment, ResourceLocation mountId, List<Candidate> updated) {
        if (playerInv.player.level().isClientSide) {
            this.target = target;
            this.equipment = equipment.copy();
            replaceCandidates(mountId, updated);
        }
    }

    private void syncCandidates() {
        if (playerInv.player instanceof ServerPlayer player) {
            ModMessages.sendToPlayer(new SyncEquipmentCandidatesPacket(containerId, target, equipment, mountId, candidates), player);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (playerInv.player.level().isClientSide || !stillValid(playerInv.player)) return;
        if (!mountId.equals(NO_MOUNT) && (equipment.isEmpty()
                || (!target.equals(EquipmentTarget.NONE) && target.resolve(playerInv.player) != equipment)
                || !(equipment.getItem() instanceof ModularEquipment modular)
                || modular.getMountDefinition(equipment, mountId).isEmpty())) {
            equipment = ItemStack.EMPTY;
            target = EquipmentTarget.NONE;
            replaceCandidates(NO_MOUNT, List.of());
            syncCandidates();
            return;
        }
        List<Candidate> updated = collectCandidates(playerInv, equipment, mountId);
        if (!sameCandidates(candidates, updated)) {
            replaceCandidates(mountId, updated);
            syncCandidates();
        }
    }

    private static boolean sameCandidates(List<Candidate> current, List<Candidate> updated) {
        if (current.size() != updated.size()) return false;
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).inventorySlot() != updated.get(i).inventorySlot()
                    || !ItemStack.matches(current.get(i).stack(), updated.get(i).stack())) return false;
        }
        return true;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        // Cards are display entries, not container slots.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player == playerInv.player && player.isAlive() && !player.isSpectator();
    }
}
