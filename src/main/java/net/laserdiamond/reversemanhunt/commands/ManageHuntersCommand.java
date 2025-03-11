package net.laserdiamond.reversemanhunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.reversemanhunt.RMGame;
import net.laserdiamond.reversemanhunt.api.event.SpeedRunnerToHunterEvent;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunner;
import net.laserdiamond.reversemanhunt.event.ForgeServerEvents;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.game.RemainingPlayerCountS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterCapabilitySyncS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterChangeS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Command for adding and removing Hunters
 */
public class ManageHuntersCommand {

    private static final int PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("hunters")
                        .requires(commandSourceStack -> ForgeServerEvents.permission(commandSourceStack, PERMISSION_LEVEL))
                        .then(
                               Commands.literal("add")
                                       .executes(commandContext -> modifyHunters(commandContext, Collections.singleton(commandContext.getSource().getPlayerOrException()), Modifier.ADD, false))
                                       .then(
                                               Commands.argument("target", EntityArgument.players())
                                                       .executes(commandContext -> modifyHunters(commandContext, EntityArgument.getPlayers(commandContext, "target"), Modifier.ADD, false))
                                                       .then(
                                                               Commands.argument("isBuffed", BoolArgumentType.bool())
                                                                       .executes(commandContext -> modifyHunters(commandContext, EntityArgument.getPlayers(commandContext, "target"), Modifier.ADD, BoolArgumentType.getBool(commandContext, "isBuffed")))
                                                       )
                                       )
                        )
                        .then(
                                Commands.literal("remove")
                                        .executes(commandContext -> modifyHunters(commandContext, Collections.singleton(commandContext.getSource().getPlayerOrException()), Modifier.REMOVE, false))
                                        .then(
                                                Commands.argument("target", EntityArgument.players())
                                                        .executes(commandContext -> modifyHunters(commandContext, EntityArgument.getPlayers(commandContext, "target"), Modifier.REMOVE, false))
                                        )
                        )
        );
    }

    private enum Modifier
    {
        ADD,
        REMOVE
    }

    private enum Reason
    {
        ALREADY_HUNTER,
        NOT_ALREADY_HUNTER,
        GAME_RUNNING
    }

    private static void logHunterChange(CommandSourceStack source, ServerPlayer serverPlayer, Modifier modifier, boolean isBuffed)
    {
        switch (modifier)
        {
            case ADD ->
            {
                if (source.getEntity() == serverPlayer)
                {
                    if (isBuffed)
                    {
                        source.sendSuccess(() -> Component.literal("Set self as a " + ChatFormatting.DARK_RED + "Buffed Hunter"), true);
                    } else
                    {
                        source.sendSuccess(() -> Component.literal("Set self as a " + ChatFormatting.DARK_RED + "Hunter"), true);
                    }

                } else
                {
                    if (source.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK))
                    {
                        if (isBuffed)
                        {
                            serverPlayer.sendSystemMessage(Component.literal("You are now a " + ChatFormatting.DARK_RED + "Buffed Hunter"));
                        } else
                        {
                            serverPlayer.sendSystemMessage(Component.literal("You are now a " + ChatFormatting.DARK_RED + "Hunter"));
                        }
                    }

                    if (isBuffed)
                    {
                        source.sendSuccess(() -> Component.literal("Set " + ChatFormatting.BLUE + serverPlayer.getName().getString() + ChatFormatting.WHITE + " to be a " + ChatFormatting.DARK_RED + "Buffed Hunter"), true);
                    } else
                    {
                        source.sendSuccess(() -> Component.literal("Set " + ChatFormatting.BLUE + serverPlayer.getName().getString() + ChatFormatting.WHITE + " to be a " + ChatFormatting.DARK_RED + "Hunter"), true);
                    }
                }
            }
            case REMOVE ->
            {
                if (source.getEntity() == serverPlayer)
                {
                    source.sendSuccess(() -> Component.literal("Set self to not be a " + ChatFormatting.DARK_RED + "Hunter"), true);
                } else
                {
                    if (source.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK))
                    {
                        serverPlayer.sendSystemMessage(Component.literal("You are no longer a " + ChatFormatting.DARK_RED + "Hunter"));
                    }

                    source.sendSuccess(() -> Component.literal("Set " + ChatFormatting.BLUE + serverPlayer.getName().getString() + ChatFormatting.WHITE + " to not be a " + ChatFormatting.DARK_RED + "Hunter"), true);
                }
            }
        }
    }

    private static void logFailChange(CommandSourceStack source, ServerPlayer serverPlayer, Reason reason)
    {
        switch (reason)
        {
            case ALREADY_HUNTER -> source.sendFailure(Component.literal(ChatFormatting.BLUE + serverPlayer.getName().getString() + ChatFormatting.RED + " is already a hunter!"));
            case NOT_ALREADY_HUNTER -> source.sendFailure(Component.literal(ChatFormatting.BLUE + serverPlayer.getName().getString() + ChatFormatting.RED + " is already not a hunter!"));
            case GAME_RUNNING -> source.sendFailure(Component.literal(ChatFormatting.RED + "A Reverse Manhunt game is currently in progress. Please pause or stop the game to add/remove hunters"));
        }
    }

    private static int modifyHunters(CommandContext<CommandSourceStack> commandContext, Collection<ServerPlayer> players, Modifier modifier, boolean isBuffed)
    {
        AtomicInteger i = new AtomicInteger();

        if (RMGame.State.isGameRunning())
        {
            logFailChange(commandContext.getSource(), null, Reason.GAME_RUNNING); // Log fail. Okay to pass null, since the ServerPlayer isn't used because of the Reason
            return 0; // End command. Do not modify hunters if game is running
        }

        switch (modifier)
        {
            case ADD -> // Adding hunters
            {
                for (ServerPlayer serverPlayer : players)
                {
                    serverPlayer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                    {
                        if (playerHunter.isHunter())
                        {
                            logFailChange(commandContext.getSource(), serverPlayer, Reason.ALREADY_HUNTER);
                            return; // Exit if player is already a hunter
                        }
//                        playerHunter.setHunter(true); // Now a hunter
//                        playerHunter.setBuffed(isBuffed); // Set if buffed
//                        RMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter), serverPlayer);
//                        RMPackets.sendToAllTrackingEntityAndSelf(new HunterCapabilitySyncS2CPacket(serverPlayer.getId(), playerHunter.toNBT()), serverPlayer);
                        MinecraftForge.EVENT_BUS.post(new SpeedRunnerToHunterEvent(serverPlayer, isBuffed, false));
                        logHunterChange(commandContext.getSource(), serverPlayer, modifier, isBuffed);
                        i.getAndIncrement();
                    });
                }
                return i.get();
            }
            case REMOVE -> // Removing hunters
            {
                for (ServerPlayer serverPlayer : players)
                {
                    serverPlayer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                    {
                        if (!playerHunter.isHunter())
                        {
                            logFailChange(commandContext.getSource(), serverPlayer, Reason.NOT_ALREADY_HUNTER);
                            return; // Exit if player is already not a hunter
                        }
                        playerHunter.setHunter(false); // No longer a hunter
                        playerHunter.setBuffed(false); // No longer buffed
                        RMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter), serverPlayer);
                        RMPackets.sendToAllTrackingEntityAndSelf(new HunterCapabilitySyncS2CPacket(serverPlayer.getId(), playerHunter.toNBT()), serverPlayer);
                        logHunterChange(commandContext.getSource(), serverPlayer, modifier, isBuffed);
                        i.getAndIncrement();
                    });
                }
                return i.get();
            }
        }
        RMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket(PlayerSpeedRunner.getRemainingSpeedRunners().size(), PlayerHunter.getHunters().size()));
        return 0;
    }
}
