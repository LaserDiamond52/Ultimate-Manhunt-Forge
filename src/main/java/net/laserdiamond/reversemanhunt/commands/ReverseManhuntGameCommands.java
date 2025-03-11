package net.laserdiamond.reversemanhunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.reversemanhunt.RMGame;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunner;
import net.laserdiamond.reversemanhunt.event.ForgeServerEvents;
import net.laserdiamond.reversemanhunt.api.event.ReverseManhuntGameStateEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;

/**
 * Command for starting, stopping, pausing, and resuming the Reverse Manhunt Game
 */
public class ReverseManhuntGameCommands {

    private static final int PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("reverse_manhunt")
                        .requires(commandSourceStack -> ForgeServerEvents.permission(commandSourceStack, PERMISSION_LEVEL))
                        .then(
                                Commands.literal("start")
                                        .executes(commandContext -> changeGameState(commandContext, RMGame.State.STARTED))
                        )
                        .then(
                                Commands.literal("pause")
                                        .executes(commandContext -> changeGameState(commandContext, RMGame.State.PAUSED))
                        )
                        .then(
                                Commands.literal("resume")
                                        .executes(commandContext -> changeGameState(commandContext, RMGame.State.IN_PROGRESS))
                        )
                        .then(
                                Commands.literal("stop")
                                        .executes(commandContext -> changeGameState(commandContext, RMGame.State.NOT_STARTED))
                        )
        );
    }

    private static void logGameStateChange(CommandSourceStack source, RMGame.State newGameState, boolean successful)
    {
        if (successful)
        {
            source.sendSuccess(() -> Component.literal("Set the current state of the " + ChatFormatting.GOLD + "Reverse Manhunt Game" + ChatFormatting.WHITE + " to " + ChatFormatting.AQUA + newGameState.toString()), true);

            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
            {
                switch (newGameState)
                {
                    case STARTED -> player.sendSystemMessage(Component.literal("The " + ChatFormatting.GOLD + "Reverse Manhunt Game" + ChatFormatting.WHITE + " has started!"));
                    case IN_PROGRESS -> player.sendSystemMessage(Component.literal("The " + ChatFormatting.GOLD + "Reverse Manhunt Game" + ChatFormatting.WHITE + " has resumed!"));
                    case PAUSED -> player.sendSystemMessage(Component.literal("The " + ChatFormatting.GOLD + "Reverse Manhunt Game" + ChatFormatting.WHITE + " has been paused!"));
                    case NOT_STARTED -> player.sendSystemMessage(Component.literal("The " + ChatFormatting.GOLD + "Reverse Manhunt Game" + ChatFormatting.WHITE + " was forcefully ended!"));
                }
            }
        } else
        {
            source.sendFailure(Component.literal(ChatFormatting.RED + "Cannot change the current state of the " + ChatFormatting.GOLD + "Reverse Manhunt Game" + ChatFormatting.RED + " from " + ChatFormatting.AQUA + RMGame.getCurrentGameState().toString() + ChatFormatting.RED + " to " + ChatFormatting.AQUA + newGameState.toString()));
        }
    }

    private static void logFailStart(CommandSourceStack source, RMGame.State newGameState)
    {
        switch (newGameState)
        {
            case STARTED -> source.sendFailure(Component.literal(ChatFormatting.RED + "Cannot start the game because there are no Hunters!"));
            case IN_PROGRESS -> source.sendFailure(Component.literal(ChatFormatting.RED + "Cannot resume the game because there are no Hunters!"));
        }
    }

    private static int changeGameState(CommandContext<CommandSourceStack> commandContext, RMGame.State newGameState)
    {
        int i = 0;

        List<Player> hunters = PlayerHunter.getHunters();
        List<Player> speedRunners = PlayerSpeedRunner.getRemainingSpeedRunners();
//        switch (newGameState)
//        {
//            case STARTED, IN_PROGRESS ->
//            {
//                if (hunters.isEmpty()) // Are there any hunters?
//                {
//                    logFailStart(commandContext.getSource(), newGameState); // No hunters. Game cannot start/resume
//                    return i;
//                }
//            }
//        }

        if (RMGame.setCurrentGameState(newGameState)) // Check that the game state has changed
        {
            switch (newGameState)
            {
                case STARTED -> MinecraftForge.EVENT_BUS.post(new ReverseManhuntGameStateEvent.Start(
                        hunters,
                        speedRunners
                ));
                case IN_PROGRESS -> MinecraftForge.EVENT_BUS.post(new ReverseManhuntGameStateEvent.Resume(
                        hunters,
                        speedRunners
                ));
                case PAUSED -> MinecraftForge.EVENT_BUS.post(new ReverseManhuntGameStateEvent.Pause(
                        hunters,
                        speedRunners
                ));
                case NOT_STARTED -> MinecraftForge.EVENT_BUS.post(new ReverseManhuntGameStateEvent.End(
                        ReverseManhuntGameStateEvent.End.Reason.COMMAND,
                        hunters,
                        speedRunners
                ));
            }
            logGameStateChange(commandContext.getSource(), newGameState, true);
            i++;
        } else
        {
            logGameStateChange(commandContext.getSource(), newGameState, false);
        }

        return i;
    }

}
