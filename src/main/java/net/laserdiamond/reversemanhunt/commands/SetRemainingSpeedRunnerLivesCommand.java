package net.laserdiamond.reversemanhunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.reversemanhunt.RMGame;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunner;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.event.ForgeServerEvents;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerCapabilitySyncS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerChangeS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Command used to configure the current amount of lives a speed runner has
 */
public class SetRemainingSpeedRunnerLivesCommand {

    private static final int PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("speed_runners_lives")
                        .requires(sourceStack -> ForgeServerEvents.permission(sourceStack, PERMISSION_LEVEL))
                        .then(
                                Commands.argument("target", EntityArgument.players())
                                        .then(
                                                Commands.literal("add")
                                                        .then(
                                                                Commands.argument("newLives", IntegerArgumentType.integer(1, PlayerSpeedRunner.getMaxLives()))
                                                                        .executes(commandContext -> modifySpeedRunnerLives(commandContext, EntityArgument.getPlayers(commandContext, "target"), Modifier.ADD, IntegerArgumentType.getInteger(commandContext, "newLives")))
                                                        )
                                        )
                                        .then(
                                                Commands.literal("set")
                                                        .then(
                                                                Commands.argument("newLives", IntegerArgumentType.integer(1, PlayerSpeedRunner.getMaxLives()))
                                                                        .executes(commandContext -> modifySpeedRunnerLives(commandContext, EntityArgument.getPlayers(commandContext, "target"), Modifier.SET, IntegerArgumentType.getInteger(commandContext, "newLives")))
                                                        )
                                        )
                                        .then(
                                                Commands.literal("remove")
                                                        .then(
                                                                Commands.argument("newLives", IntegerArgumentType.integer(1, PlayerSpeedRunner.getMaxLives()))
                                                                        .executes(commandContext -> modifySpeedRunnerLives(commandContext, EntityArgument.getPlayers(commandContext, "target"), Modifier.REMOVE, IntegerArgumentType.getInteger(commandContext, "newLives")))
                                                        )
                                        )
                        )
        );
    }

    private static void logLifeChange(CommandSourceStack sourceStack, ServerPlayer serverPlayer, int lives)
    {
        sourceStack.sendSuccess(() -> Component.literal("Set the speed runner lives of " + serverPlayer.getName().getString() + " to " + lives), true);
    }

    private static void logFailLifeChange(CommandSourceStack sourceStack)
    {
        sourceStack.sendFailure(Component.literal(ChatFormatting.RED + "Cannot modify speed runner lives when a game hasn't been started, or while a game is running. Please pause the game if you wish to modify the life counts of speed runners"));
    }

    private static int modifySpeedRunnerLives(CommandContext<CommandSourceStack> commandContext, Collection<ServerPlayer> players, Modifier modifier, int lives)
    {
        AtomicInteger i = new AtomicInteger();
        if (RMGame.getCurrentGameState() != RMGame.State.PAUSED)
        {
            logFailLifeChange(commandContext.getSource());
            return 0;
        }
        for (ServerPlayer serverPlayer : players)
        {
            serverPlayer.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
            {
                switch (modifier)
                {
                    case ADD ->
                    {
                        int newLives = Math.min(playerSpeedRunner.getLives() + lives, PlayerSpeedRunner.getMaxLives()); // Do not allow players to have more than the max amount of lives
                        playerSpeedRunner.setLives(newLives);
                    }
                    case SET ->
                    {
                        playerSpeedRunner.setLives(lives);
                    }
                    case REMOVE ->
                    {
                        int newLives = Math.max(1, playerSpeedRunner.getLives() - lives); // Do not allow players to have 0 lives
                        playerSpeedRunner.setLives(newLives);
                    }
                }
                RMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), serverPlayer);
                RMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(serverPlayer.getId(), playerSpeedRunner.toNBT()), serverPlayer);
                logLifeChange(commandContext.getSource(), serverPlayer, playerSpeedRunner.getLives());
                i.getAndIncrement();
            });
        }
        return i.get();
    }

    private enum Modifier
    {
        ADD,
        SET,
        REMOVE;
    }
}
