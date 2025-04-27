package net.laserdiamond.ultimatemanhunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Command used to configure the maximum amount of lives speed runners will have for the Manhunt game
 */
public class MaxSpeedRunnerLivesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("speed_runner_max_lives")
                        .requires(UltimateManhunt::hasPermission)
                        .then(
                                Commands.argument("amount", IntegerArgumentType.integer(1, UMPlayer.MAX_LIVES))
                                        .executes(commandContext -> modifyMaxLives(commandContext, IntegerArgumentType.getInteger(commandContext, "amount")))
                        )
        );
    }

    private static void logMaxLifeChange(CommandSourceStack sourceStack, int maxLives)
    {
        sourceStack.sendSuccess(() -> Component.literal("Maximum speed runner lives has been set to: " + maxLives), true);
    }

    private static void logFailMaxLifeChange(CommandSourceStack sourceStack)
    {
        sourceStack.sendFailure(Component.literal(ChatFormatting.RED + "Cannot change the maximum amount of speed runner lives when a game has already been started!"));
    }

    private static int modifyMaxLives(CommandContext<CommandSourceStack> commandContext, int maxLives)
    {
        int i = 0;
        if (UMPlayer.setMaxLives(maxLives))
        {
            logFailMaxLifeChange(commandContext.getSource());
            return i;
        }
        logMaxLifeChange(commandContext.getSource(), maxLives);
        i++;
        return i;
    }
}
