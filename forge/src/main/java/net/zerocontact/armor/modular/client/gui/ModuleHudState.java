package net.zerocontact.armor.modular.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.client.network.c2s.ModuleActionPacket;
import net.zerocontact.armor.modular.model.*;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.network.ModMessages;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ModuleHudState {
    public static final ModuleHudState INSTANCE = new ModuleHudState();
    public record Address(EquipmentTarget target, ResourceLocation mountId) {}
    public record Entry(Address address, Component name, ModuleView view,
                        ResourceLocation equipmentItem, ResourceLocation moduleItem) {
        public Component stateLabel() {
            ResourceLocation id = view.stateId();
            return Component.translatable("state." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
        }
    }
    private boolean opened;
    private List<Entry> entries = List.of();
    private int selected;
    private ModuleHudState() {}
    public boolean isOpened() { return opened; }
    public List<Entry> entries() { return entries; }
    public int selectedIndex() { return selected; }
    public void close() { opened = false; entries = List.of(); selected = 0; }
    public void toggle() {
        if (opened) close();
        else { opened = true; refresh(); }
    }
    public void refresh() {
        var player = Minecraft.getInstance().player;
        if (player == null) { close(); return; }
        Address previous = entries.isEmpty() ? null : entries.get(selected).address();
        List<Entry> next = new ArrayList<>();
        ModuleQuery.streamMounted(player)
                .sorted(Comparator.comparing((ModuleQuery.MountedModuleRef ref) -> ref.equipmentTarget().slot())
                        .thenComparingInt(ref -> ref.equipmentTarget().index())
                        .thenComparing(ModuleQuery.MountedModuleRef::mountId))
                .forEach(ref -> ModuleRegistry.getController(ref.stack()).ifPresent(controller -> {
                    ModuleContext context = new ModuleContext(player, ref.equipmentTarget(), ref.mountId(), ref.stack());
                    controller.inspect(context).ifPresent(view -> next.add(new Entry(
                            new Address(ref.equipmentTarget(), ref.mountId()), ref.stack().getHoverName().copy(), view,
                            ForgeRegistries.ITEMS.getKey(ref.equipmentTarget().resolve(player).getItem()),
                            ForgeRegistries.ITEMS.getKey(ref.stack().getItem()))));
                }));
        entries = List.copyOf(next);
        selected = Math.min(selected, Math.max(0, entries.size() - 1));
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).address().equals(previous)) { selected = i; break; }
        }
    }
    public void move(int direction) {
        if (!entries.isEmpty()) selected = Math.floorMod(selected + direction, entries.size());
    }
    public void activate() {
        if (entries.isEmpty()) return;
        Entry entry = entries.get(selected);
        entry.view().primaryActionId().ifPresent(id -> entry.view().actions().stream()
                .filter(action -> action.actionId().equals(id) && action.available())
                .findFirst().ifPresent(action -> ModMessages.sendToServer(new ModuleActionPacket(
                        entry.address().target(), entry.address().mountId(),
                        entry.equipmentItem(), entry.moduleItem(), action.actionId()))));
    }
}
