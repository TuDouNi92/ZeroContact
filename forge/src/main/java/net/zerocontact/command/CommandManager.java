package net.zerocontact.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CommandManager {

    private static final String STAMINA_COMMAND = "stamina";
    private static final String STAMINA_MSG = "Enable Stamina:";
    private static final String DOGTAG_COMMAND = "dogtag";
    private static final String DOGTAG_MSG = "Enable Dogtag drop:";
    private static final String EXP_BALLISTIC_COMMAND = "experimentalBallistic";
    private static final String EXP_BALLISTIC_MSG = "Enable ExperimentalBallistic feature:";

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

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(STAMINA_COMMAND)
                .requires(commandSourceStack -> Optional.ofNullable(commandSourceStack.getPlayer()).isPresent() && commandSourceStack.hasPermission(2))
                .then(Commands.argument("boolean", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean isEnabledStamina = context.getArgument("boolean", Boolean.class);
                            CommandSavedData data = CommandSavedData.get(context.getSource().getLevel());
                            data.setStaminaState(isEnabledStamina);
                            Component message = Component.literal(STAMINA_MSG)
                                    .withStyle(ChatFormatting.GOLD)
                                    .append(Component.literal(String.valueOf(isEnabledStamina)).withStyle(isEnabledStamina ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));
                            context.getSource().sendSuccess(() -> message, true);
                            return Command.SINGLE_SUCCESS;
                        }))

        );
        dispatcher.register(Commands.literal(DOGTAG_COMMAND)
                .requires(commandSourceStack ->
                        Optional.ofNullable(commandSourceStack.getPlayer()).isPresent() && commandSourceStack.hasPermission(2))
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
                        Optional.ofNullable(commandSourceStack.getPlayer()).isPresent() && commandSourceStack.hasPermission(2))
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
    }
}
