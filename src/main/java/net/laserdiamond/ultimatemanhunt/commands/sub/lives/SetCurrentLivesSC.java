package net.laserdiamond.ultimatemanhunt.commands.sub.lives;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public final class SetCurrentLivesSC extends UltimateManhuntCommands.SubCommand {

    public SetCurrentLivesSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder) {
        super(argumentBuilder
                .then(
                        Commands.literal("lives")
                                .then(
                                        Commands.literal("setCurrent")
                                                .then(
                                                        Commands.argument("target", EntityArgument.players())
                                                                .then(
                                                                        Commands.literal("add")
                                                                                .then(
                                                                                        Commands.argument("newLives", IntegerArgumentType.integer(1, UMPlayer.getMaxLives()))
                                                                                                .executes(commandContext -> modifySpeedRunnerLives(commandContext, EntityArgument.getPlayers(commandContext, "target"), Modifier.ADD, IntegerArgumentType.getInteger(commandContext, "newLives")))
                                                                                )
                                                                )
                                                                .then(
                                                                        Commands.literal("set")
                                                                                .then(
                                                                                        Commands.argument("newLives", IntegerArgumentType.integer(1, UMPlayer.getMaxLives()))
                                                                                                .executes(commandContext -> modifySpeedRunnerLives(commandContext, EntityArgument.getPlayers(commandContext, "target"), Modifier.SET, IntegerArgumentType.getInteger(commandContext, "newLives")))
                                                                                )
                                                                )
                                                                .then(
                                                                        Commands.literal("remove")
                                                                                .then(
                                                                                        Commands.argument("newLives", IntegerArgumentType.integer(1, UMPlayer.getMaxLives()))
                                                                                                .executes(commandContext -> modifySpeedRunnerLives(commandContext, EntityArgument.getPlayers(commandContext, "target"), Modifier.REMOVE, IntegerArgumentType.getInteger(commandContext, "newLives")))
                                                                                )
                                                                )
                                                )
                                )
                ));
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
        if (UMGame.getCurrentGameState() != UMGame.State.PAUSED)
        {
            logFailLifeChange(commandContext.getSource());
            return 0;
        }
        for (ServerPlayer serverPlayer : players)
        {
            serverPlayer.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                switch (modifier)
                {
                    case ADD ->
                    {
                        umPlayer.setLives(umPlayer.getLives() + lives);
                    }
                    case SET ->
                    {
                        umPlayer.setLives(lives);
                    }
                    case REMOVE ->
                    {
                        int newLives = Math.max(1, umPlayer.getLives() - lives); // Do not allow players to have 0 lives
                        umPlayer.setLives(newLives);
                    }
                }
                umPlayer.sendUpdateFromServerToSelf(serverPlayer);
                logLifeChange(commandContext.getSource(), serverPlayer, umPlayer.getLives());
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
