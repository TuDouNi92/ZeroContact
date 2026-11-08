package net.zerocontact.armor.modular.module.radio.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.armor.modular.ModuleQuery;
import net.zerocontact.armor.modular.client.network.c2s.ModuleDataPacket;
import net.zerocontact.armor.modular.model.EquipmentTarget;
import net.zerocontact.armor.modular.module.radio.api.MBITR;
import net.zerocontact.armor.modular.module.radio.container.RadioContainer;
import net.zerocontact.armor.modular.module.radio.model.RadioProfile;
import net.zerocontact.armor.modular.module.radio.service.MBITRController;
import net.zerocontact.capability.CapabilityRegistries;
import net.zerocontact.network.ModMessages;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RadioScreen extends Screen {
    private static final int MAX_DIGITS = 4;
    private static final int MAX_GROUPS = 16;
    private static final int PANEL_WIDTH = 206;
    private static final int PANEL_HEIGHT = 236;

    private final EquipmentTarget target;
    private final ResourceLocation mountId;
    private final ResourceLocation equipmentItem;
    private final ResourceLocation moduleItem;
    private final List<Integer> groupChannels;
    private int homeChannel;
    private int groupIndex;
    private boolean groupMode;
    private String input = "";
    private String status = "";

    private RadioScreen(ModuleQuery.MountedModuleRef ref, ResourceLocation equipmentItem,
                        ResourceLocation moduleItem, RadioProfile profile) {
        super(Component.literal("MBITR"));
        this.target = ref.equipmentTarget();
        this.mountId = ref.mountId();
        this.equipmentItem = equipmentItem;
        this.moduleItem = moduleItem;
        this.homeChannel = profile.homeChannel();
        this.groupChannels = new ArrayList<>(profile.subChannels());
    }

    public static void openFirstActive(Minecraft minecraft) {
        var player = minecraft.player;
        if (player == null) return;
        ModuleQuery.streamMounted(player).filter(ref -> ref.stack()
                .getCapability(CapabilityRegistries.RADIO)
                .map(radio -> radio.getRadioState().radioActivated()).orElse(false))
                .findFirst().ifPresent(ref -> ref.stack().getCapability(CapabilityRegistries.RADIO).ifPresent(radio -> {
                    var equipment = ref.equipmentTarget().resolve(player);
                    if (equipment.isEmpty()) return;
                    minecraft.setScreen(new RadioScreen(ref,
                            ForgeRegistries.ITEMS.getKey(equipment.getItem()),
                            ForgeRegistries.ITEMS.getKey(ref.stack().getItem()), radio.getProfile()));
                }));
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        String[][] labels = {{"1", "2", "3", "↑"}, {"4", "5", "6", "↓"},
                {"7", "8", "9", "GR"}, {"ESC", "0", "ENT", "×"}};
        for (int row = 0; row < labels.length; row++) {
            for (int col = 0; col < labels[row].length; col++) {
                String label = labels[row][col];
                int x = left + 12 + col * 47;
                int y = top + 88 + row * 32;
                addRenderableWidget(Button.builder(Component.literal(label), button -> press(label))
                        .bounds(x, y, 42, 27).build());
            }
        }
    }

    private void press(String label) {
        switch (label) {
            case "GR" -> {
                groupMode = !groupMode;
                input = "";
                status = "";
            }
            case "↑" -> browse(-1);
            case "↓" -> browse(1);
            case "ENT" -> submit();
            case "ESC" -> {
                input = "";
                status = "";
            }
            case "×" -> onClose();
            default -> appendDigit(label);
        }
    }

    private void appendDigit(String digit) {
        if (input.length() < MAX_DIGITS) {
            input += digit;
            status = "";
        }
    }

    private void browse(int direction) {
        groupMode = true;
        groupIndex = Math.floorMod(groupIndex + direction, groupChannels.size() + 1);
        input = "";
        status = "";
    }

    private void submit() {
        if (input.isEmpty()) return;
        int channel = Integer.parseInt(input);
        if (channel < MBITRController.MIN_FREQUENCY || channel > MBITRController.MAX_FREQUENCY) {
            status = "30.0-512.0 MHz only";
            return;
        }
        if (groupMode && homeChannel == 0) {
            status = "Set MAIN channel first";
            return;
        }
        if (groupMode) {
            if (groupIndex == groupChannels.size()) {
                if (groupChannels.size() >= MAX_GROUPS) {
                    status = "Group list is full";
                    return;
                }
                groupChannels.add(channel);
            } else {
                groupChannels.set(groupIndex, channel);
            }
        } else {
            homeChannel = channel;
        }
        CompoundTag payload = new CompoundTag();
        payload.putInt(RadioContainer.NBT_PROFILE_CHANNEL, homeChannel);
        ListTag groups = new ListTag();
        for (int group : groupChannels) groups.add(IntTag.valueOf(group));
        payload.put(RadioContainer.NBT_SUB_CHANNEL, groups);
        ModMessages.sendToServer(new ModuleDataPacket(target, mountId, equipmentItem,
                moduleItem, MBITRController.SET_PROFILE_OPERATION, payload));
        input = "";
        status = "Sent";
    }

    @Override
    public void tick() {
        if (minecraft == null || minecraft.player == null || activeRadio().isEmpty()) onClose();
    }

    private Optional<MBITR> activeRadio() {
        ItemStack equipment = null;
        if (minecraft != null) {
            equipment = target.resolve(minecraft.player);
        }
        if (equipment != null && (equipment.isEmpty() || !equipmentItem.equals(ForgeRegistries.ITEMS.getKey(equipment.getItem())))) {
            return Optional.empty();
        }
        if (equipment != null) {
            return equipment.getCapability(CapabilityRegistries.MODULAR_EQUIPMENT).resolve()
                    .map(container -> container.getModule(mountId))
                    .filter(stack -> !stack.isEmpty() && moduleItem.equals(ForgeRegistries.ITEMS.getKey(stack.getItem())))
                    .flatMap(stack -> stack.getCapability(CapabilityRegistries.RADIO).resolve())
                    .filter(radio -> radio.getRadioState().radioActivated());
        }
        return Optional.empty();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            appendDigit(Integer.toString(keyCode - GLFW.GLFW_KEY_0));
            return true;
        }
        if (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            appendDigit(Integer.toString(keyCode - GLFW.GLFW_KEY_KP_0));
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submit();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            browse(keyCode == GLFW.GLFW_KEY_UP ? -1 : 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
            input = "";
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !input.isEmpty()) {
            input = "";
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFF20282D);
        graphics.fill(left + 8, top + 27, left + PANEL_WIDTH - 8, top + 78, 0xFF9CAF91);
        graphics.drawCenteredString(font, "MBITR", width / 2, top + 9, 0xFFE4E8DC);
        String mode = groupMode ? "GR " + (groupIndex + 1) + "/" + (groupChannels.size() + 1) : "MAIN";
        graphics.drawString(font, mode, left + 16, top + 35, 0xFF25352C, false);
        int channel = groupMode ? (groupIndex < groupChannels.size() ? groupChannels.get(groupIndex) : 0) : homeChannel;
        graphics.drawString(font, formatFrequency(input.isEmpty() ? channel : Integer.parseInt(input))
                        + (input.isEmpty() ? "" : "_") + " MHz",
                left + 16, top + 52, 0xFF172A1E, false);
        if (!status.isEmpty()) graphics.drawCenteredString(font, status, width / 2, top + 220, 0xFFE0DFB0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static String formatFrequency(int channel) {
        return channel / 10 + "." + channel % 10;
    }
}
