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
    public static class CommandSavedData extends SavedData {
        public boolean staminaState = false;
        public boolean dogTagState = false;
        public boolean experimentalBallistic = false;
        CommandSavedData() {
        }

        public static CommandSavedData load(CompoundTag compoundTag) {
            CommandSavedData data = new CommandSavedData();
            data.staminaState = compoundTag.getBoolean("staminaState");
            data.dogTagState = compoundTag.getBoolean("dogTagState");
            data.experimentalBallistic = compoundTag.getBoolean("experimentalBallistic");
            return data;
        }

        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
            compoundTag.putBoolean("staminaState", staminaState);
            compoundTag.putBoolean("dogTagState", dogTagState);
            compoundTag.putBoolean("experimentalBallistic", experimentalBallistic);
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

        public void setExperimentalBallistic(boolean experimentalBallistic){
            this.experimentalBallistic = experimentalBallistic;
            setDirty();
        }

        public static CommandSavedData get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                    CommandSavedData::load,
                    CommandSavedData::new,
                    "zerocontact_command_state"
            );
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stamina")
                .requires(commandSourceStack -> Optional.ofNullable(commandSourceStack.getPlayer()).isPresent() && commandSourceStack.hasPermission(2))
                .then(Commands.argument("boolean", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean isEnabledStamina = context.getArgument("boolean", Boolean.class);
                            CommandSavedData data = CommandSavedData.get(context.getSource().getLevel());
                            data.setStaminaState(isEnabledStamina);
                            Component message = Component.literal("Enable Stamina:")
                                    .withStyle(ChatFormatting.GOLD)
                                    .append(Component.literal(String.valueOf(isEnabledStamina)).withStyle(isEnabledStamina ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));
                            context.getSource().sendSuccess(() -> message, true);
                            return Command.SINGLE_SUCCESS;
                        }))

        );
        dispatcher.register(Commands.literal("dogtag")
                .requires(commandSourceStack ->
                        Optional.ofNullable(commandSourceStack.getPlayer()).isPresent() && commandSourceStack.hasPermission(2))
                .then(Commands.argument("boolean", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean isEnabledDogTag = context.getArgument("boolean", Boolean.class);
                            CommandSavedData data = CommandSavedData.get(context.getSource().getLevel());
                            data.setDogTagState(isEnabledDogTag);
                            Component message = Component.literal("Enable Dogtag drop:")
                                    .withStyle(ChatFormatting.GOLD)
                                    .append(Component.literal(String.valueOf(isEnabledDogTag)).withStyle(isEnabledDogTag ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));
                            context.getSource().sendSuccess(() -> message, true);
                            return Command.SINGLE_SUCCESS;
                        }))

        );

        dispatcher.register(Commands.literal("experimentalBallistic")
                .requires(commandSourceStack ->
                        Optional.ofNullable(commandSourceStack.getPlayer()).isPresent() && commandSourceStack.hasPermission(2))
                .then(Commands.argument("boolean",BoolArgumentType.bool())
                .executes(context->{
                    boolean isEnableBallistic = context.getArgument("boolean", Boolean.class);
                    CommandSavedData data = CommandSavedData.get(context.getSource().getLevel());
                    data.setExperimentalBallistic(isEnableBallistic);
                    Component message = Component.literal("Enable ExperimentalBallistic feature:")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(String.valueOf(isEnableBallistic)).withStyle(isEnableBallistic ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));
                    context.getSource().sendSuccess(()->message,true);
                    return Command.SINGLE_SUCCESS;
                }))
        );
    }
}
