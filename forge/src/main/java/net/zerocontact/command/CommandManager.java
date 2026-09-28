package net.zerocontact.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.zerocontact.ZeroContact;
import net.zerocontact.api.armor.modular.EquipmentModule;
import net.zerocontact.api.armor.modular.ModularEquipment;
import net.zerocontact.armor.modular.model.MountDefinition;
import net.zerocontact.armor.modular.registry.ModuleRegistry;
import net.zerocontact.armor.modular.service.ModuleMountService;
import net.zerocontact.armor.modular.service.ModuleSyncService;
import net.zerocontact.armor.modular.client.menu.EquipmentMenu;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandManager {

    private static final String DOGTAG_COMMAND = "dogtag";
    private static final String DOGTAG_MSG = "Enable Dogtag drop:";
    private static final String EXP_BALLISTIC_COMMAND = "experimentalBallistic";
    private static final String EXP_BALLISTIC_MSG = "Enable ExperimentalBallistic feature:";

    private static final String MODULAR_EQUIP_COMMAND = "modular";
    private static final String MODULAR_EQUIP_MSG = "Successfully edited module";
    public static final String FAILED_TO_MOUNT_MODULE_MSG = "Failed to mount module";
    public static final String NOT_A_MODULE_ITEM_MSG = "Not a module Item";
    public static final String FAILED_TO_FIND_TARGET_MSG = "Failed to find  target";

    public static class CommandSavedData extends SavedData {
        private static final String STAMINA_STATE = "staminaState";
        private static final String DOGTAG_STATE = "dogTagState";
        private static final String EXP_BALLISTIC = "experimentalBallistic";
        private static final String DATA_NAME = "zerocontact_command_state";
        public boolean staminaState = false;
        public boolean dogTagState = false;
        public boolean experimentalBallistic = true;

        CommandSavedData() {
        }

        public static CommandSavedData load(CompoundTag compoundTag) {
            CommandSavedData data = new CommandSavedData();
            data.staminaState = compoundTag.getBoolean(STAMINA_STATE);
            data.dogTagState = compoundTag.getBoolean(DOGTAG_STATE);
            data.experimentalBallistic = compoundTag.getBoolean(EXP_BALLISTIC);
            return data;
        }

        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
            compoundTag.putBoolean(STAMINA_STATE, staminaState);
            compoundTag.putBoolean(DOGTAG_STATE, dogTagState);
            compoundTag.putBoolean(EXP_BALLISTIC, experimentalBallistic);
            return compoundTag;
        }

        public void setStaminaState(boolean staminaState) {
            this.staminaState = staminaState;
            setDirty();
        }

        public void setDogTagState(boolean dogTagState) {
            this.dogTagState = dogTagState;
            setDirty();
        }

        public void setExperimentalBallistic(boolean experimentalBallistic) {
            this.experimentalBallistic = experimentalBallistic;
            setDirty();
        }

        public static CommandSavedData get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                    CommandSavedData::load,
                    CommandSavedData::new,
                    DATA_NAME
            );
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        dispatcher.register(Commands.literal("zerocontact")
                .then(Commands.literal("equipment")
                        .executes(context -> {
                            EquipmentMenu.open(context.getSource().getPlayerOrException(),
                                    Component.translatable("screen.zerocontact.equipment.title"));
                            return Command.SINGLE_SUCCESS;
                        })));

        dispatcher.register(Commands.literal(DOGTAG_COMMAND)
                .requires(commandSourceStack ->
                        commandSourceStack.getPlayer() != null && commandSourceStack.hasPermission(2))
                .then(Commands.argument("boolean", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean isEnabledDogTag = context.getArgument("boolean", Boolean.class);
                            CommandSavedData data = CommandSavedData.get(context.getSource().getLevel());
                            data.setDogTagState(isEnabledDogTag);
                            Component message = Component.literal(DOGTAG_MSG)
                                    .withStyle(ChatFormatting.GOLD)
                                    .append(Component.literal(String.valueOf(isEnabledDogTag)).withStyle(isEnabledDogTag ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));
                            context.getSource().sendSuccess(() -> message, true);
                            return Command.SINGLE_SUCCESS;
                        }))

        );

        dispatcher.register(Commands.literal(EXP_BALLISTIC_COMMAND)
                .requires(commandSourceStack ->
                        commandSourceStack.getPlayer() != null && commandSourceStack.hasPermission(2))
                .executes(context -> {
                    CommandSavedData data = CommandSavedData.get(context.getSource().getLevel());
                    boolean currentState = data.experimentalBallistic;
                    Component msg = Component.literal(EXP_BALLISTIC_MSG)
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(String.valueOf(currentState)).withStyle(currentState ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));
                    context.getSource().sendSuccess(() -> msg, true);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("boolean", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean isEnableBallistic = context.getArgument("boolean", Boolean.class);
                            CommandSavedData data = CommandSavedData.get(context.getSource().getLevel());
                            data.setExperimentalBallistic(isEnableBallistic);
                            Component message = Component.literal(EXP_BALLISTIC_MSG)
                                    .withStyle(ChatFormatting.GOLD)
                                    .append(Component.literal(String.valueOf(isEnableBallistic)).withStyle(isEnableBallistic ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));
                            context.getSource().sendSuccess(() -> message, true);
                            return Command.SINGLE_SUCCESS;
                        }))
        );
        dispatcher.register(Commands.literal(MODULAR_EQUIP_COMMAND)
                .requires(source -> source.getPlayer() != null && source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("equipmentSlot", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(EquipmentSlot.values()).map(EquipmentSlot::getName), builder))
                                .then(Commands.argument("mount_id", StringArgumentType.string())
                                        .suggests((ctx, builder) -> {
                                                    Entity target = EntityArgument.getEntity(ctx, "target");
                                                    if (!(target instanceof LivingEntity livingEntity)) {
                                                        return SharedSuggestionProvider.suggest(List.of(), builder);
                                                    }

                                                    String inputSlot = StringArgumentType.getString(ctx, "equipmentSlot");
                                                    EquipmentSlot equipmentSlot = Arrays.stream(EquipmentSlot.values())
                                                            .filter(s -> s.getName().equals(inputSlot))
                                                            .findFirst()
                                                            .orElseThrow(() -> {
                                                                ctx.getSource().sendFailure(Component.literal(FAILED_TO_MOUNT_MODULE_MSG));
                                                                return new NoSuchElementException();
                                                            });

                                                    ItemStack stack = livingEntity.getItemBySlot(equipmentSlot);
                                                    if (stack.getItem() instanceof ModularEquipment modularEquipment) {
                                                        return SharedSuggestionProvider.suggest(
                                                                modularEquipment.getMountDefinitions(stack).stream().map(ref -> ref.mountId().getPath()),
                                                                builder
                                                        );
                                                    }
                                                    return SharedSuggestionProvider.suggest(
                                                            List.of(),
                                                            builder
                                                    );
                                                }

                                        )
                                        .then(Commands.argument("module", ItemArgument.item(commandBuildContext))
                                                .suggests((ctx, builder) -> {
                                                    Entity target = EntityArgument.getEntity(ctx, "target");
                                                    if (!(target instanceof LivingEntity livingEntity)) {
                                                        return SharedSuggestionProvider.suggest(List.of(), builder);
                                                    }

                                                    String inputSlot = StringArgumentType.getString(ctx, "equipmentSlot");
                                                    EquipmentSlot equipmentSlot = Arrays.stream(EquipmentSlot.values())
                                                            .filter(s -> s.getName().equals(inputSlot))
                                                            .findFirst()
                                                            .orElseThrow(() -> {
                                                                ctx.getSource().sendFailure(Component.literal(FAILED_TO_MOUNT_MODULE_MSG));
                                                                return new NoSuchElementException();
                                                            });

                                                    ItemStack stack = livingEntity.getItemBySlot(equipmentSlot);

                                                    if (stack.getItem() instanceof ModularEquipment modularEquipment) {
                                                        ResourceLocation mountId = new ResourceLocation(
                                                                ZeroContact.MOD_ID,
                                                                StringArgumentType.getString(ctx, "mount_id")
                                                        );
                                                        MountDefinition def = modularEquipment.getMountDefinition(stack, mountId).orElse(null);
                                                        if (def != null) {
                                                            return SharedSuggestionProvider.suggest(
                                                                    ModuleRegistry.getModulesFor(def).stream().map(module -> ZeroContact.MOD_ID + ":" + module.getItem()),
                                                                    builder
                                                            );
                                                        }
                                                    }

                                                    return SharedSuggestionProvider.suggest(List.of(), builder);
                                                })
                                                .executes(context -> {
                                                    Entity target = EntityArgument.getEntity(context, "target");
                                                    if (!(target instanceof LivingEntity livingEntity)) {
                                                        context.getSource().sendFailure(Component.literal(FAILED_TO_FIND_TARGET_MSG));
                                                        return 0;
                                                    }

                                                    Item module = ItemArgument.getItem(context, "module").getItem();
                                                    if (!(module instanceof EquipmentModule) && module != Items.AIR) {
                                                        context.getSource().sendFailure(Component.literal(NOT_A_MODULE_ITEM_MSG));
                                                        return 0;
                                                    }

                                                    String inputSlot = StringArgumentType.getString(context, "equipmentSlot");
                                                    EquipmentSlot equipmentSlot = Arrays.stream(EquipmentSlot.values())
                                                            .filter(s -> s.getName().equals(inputSlot))
                                                            .findFirst()
                                                            .orElseThrow(() -> {
                                                                context.getSource().sendFailure(Component.literal(FAILED_TO_MOUNT_MODULE_MSG));
                                                                return new NoSuchElementException();
                                                            });

                                                    ItemStack armor = livingEntity.getItemBySlot(equipmentSlot);
                                                    ResourceLocation mountId = new ResourceLocation(
                                                            ZeroContact.MOD_ID,
                                                            StringArgumentType.getString(context, "mount_id")
                                                    );

                                                    boolean result;
                                                    if (module != Items.AIR) {
                                                        result = ModuleMountService.mount(
                                                                armor, mountId, module.getDefaultInstance());
                                                    } else {
                                                        ItemStack stack = ModuleMountService.unMount(armor, mountId);
                                                        result = !stack.isEmpty();
                                                    }

                                                    if (!result) {
                                                        context.getSource().sendFailure(Component.literal(FAILED_TO_MOUNT_MODULE_MSG));
                                                        return 0;
                                                    }

                                                    if (target instanceof Player player)
                                                        ModuleSyncService.sync(player, equipmentSlot);

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal(MODULAR_EQUIP_MSG), true);
                                                    return Command.SINGLE_SUCCESS;
                                                }))))));
    }
}
