package net.laserdiamond.ultimatemanhunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.capability.speedrunner.PlayerSpeedRunner;
import net.laserdiamond.ultimatemanhunt.event.ForgeServerEvents;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerMaxLifeChangeS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Command used to configure the maximum amount of lives speed runners will have for the Reverse Manhunt game
 */
public class MaxSpeedRunnerLivesCommand {

    private static final int PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("speed_runner_max_lives")
                        .requires(sourceStack -> ForgeServerEvents.permission(sourceStack, PERMISSION_LEVEL))
                        .then(
                                Commands.argument("amount", IntegerArgumentType.integer(1, PlayerSpeedRunner.MAX_LIVES))
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
        if (UMGame.State.hasGameBeenStarted())
        {
            logFailMaxLifeChange(commandContext.getSource());
            return 0;
        }
        PlayerSpeedRunner.setMaxLives(maxLives);
        UMPackets.sendToAllClients(new SpeedRunnerMaxLifeChangeS2CPacket(maxLives));
        logMaxLifeChange(commandContext.getSource(), maxLives);
        i++;
        return i;
    }
}
