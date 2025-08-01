package net.zerocontact.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class CommandManager {
    public static boolean isEnabledStamina = false;
    public static boolean isEnabledDogTag = true;
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stamina")
                .then(Commands.argument("toggle", StringArgumentType.word()))
                .requires(commandSourceStack -> Optional.ofNullable(commandSourceStack.getPlayer()).isPresent())
                .executes(context -> {
                    isEnabledStamina = !isEnabledStamina;
                    Component message = Component.literal("Enable Stamina:")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(String.valueOf(isEnabledStamina)));
                    context.getSource().sendSuccess(()->message,true);
                    return Command.SINGLE_SUCCESS;
                })
        );
        dispatcher.register(Commands.literal("dogtag")
                .requires(commandSourceStack ->
                    Optional.ofNullable(commandSourceStack.getPlayer()).isPresent())
                .executes(context -> {
                    isEnabledDogTag = !isEnabledDogTag;
                    Component message = Component.literal("Enable Dogtag drop:")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(String.valueOf(isEnabledDogTag)));
                    context.getSource().sendSuccess(()->message,true);
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
