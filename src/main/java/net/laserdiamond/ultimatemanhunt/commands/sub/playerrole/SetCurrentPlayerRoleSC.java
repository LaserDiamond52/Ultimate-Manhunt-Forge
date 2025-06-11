package net.laserdiamond.ultimatemanhunt.commands.sub.playerrole;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.api.event.SpeedRunnerToHunterEvent;
import net.laserdiamond.ultimatemanhunt.api.event.SpeedRunnerToSpectatorEvent;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.game.RemainingPlayerCountS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public final class SetCurrentPlayerRoleSC extends UltimateManhuntCommands.SubCommand {

    public SetCurrentPlayerRoleSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder) {
        super(argumentBuilder
                .then(
                        Commands.literal("roles")
                                .then(
                                        Commands.literal("current")
                                                .then(
                                                        Commands.argument("target", EntityArgument.players())
                                                                .then(
                                                                        Commands.literal("speed_runner")
                                                                                .executes(commandContext -> modifyRoles(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "target"), UMGame.PlayerRole.SPEED_RUNNER, false))
                                                                )
                                                                .then(
                                                                        Commands.literal("hunter")
                                                                                .executes(commandContext -> modifyRoles(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "target"), UMGame.PlayerRole.HUNTER, false))
                                                                                .then(
                                                                                        Commands.argument("is_buffed_hunter", BoolArgumentType.bool())
                                                                                                .executes(commandContext -> modifyRoles(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "target"), UMGame.PlayerRole.HUNTER, BoolArgumentType.getBool(commandContext, "is_buffed_hunter")))
                                                                                )
                                                                )
                                                                .then(
                                                                        Commands.literal("spectator")
                                                                                .executes(commandContext -> modifyRoles(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "target"), UMGame.PlayerRole.SPECTATOR, false))
                                                                )
                                                )
                                )
                ));
    }

    private static int modifyRoles(CommandSourceStack source, Collection<ServerPlayer> players, UMGame.PlayerRole playerRole, boolean isBuffedHunter)
    {
        AtomicInteger i = new AtomicInteger();

        if (UMGame.State.isGameRunning())
        {
            source.sendFailure(Component.literal(ChatFormatting.RED + "A Manhunt game is currently in progress. Please pause or stop the game to change the roles of players!"));
            return 0;
        }

        players.forEach(serverPlayer ->
        {
            serverPlayer.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                UMGame.PlayerRole oldRole = umPlayer.getRole();
                umPlayer.setRole(playerRole)
                        .setBuffedHunter(isBuffedHunter);
                if (oldRole == UMGame.PlayerRole.SPECTATOR && playerRole != UMGame.PlayerRole.SPECTATOR)
                {
                    serverPlayer.setGameMode(GameType.DEFAULT_MODE);
                }
                source.sendSuccess(() -> Component.literal("Set " + serverPlayer.getName().getString() + "'s role from " + oldRole.getAsName() + " to " + playerRole.getAsName()), true);
                if (playerRole == UMGame.PlayerRole.HUNTER)
                {
//                    MinecraftForge.EVENT_BUS.post(new SpeedRunnerToHunterEvent(serverPlayer, isBuffedHunter, false));
                    if (isBuffedHunter)
                    {
                        source.sendSuccess(() -> Component.literal(serverPlayer.getName().getString() + " has been set to be a buffed hunter"), true);
                    }
                } else
                {
//                    if (playerRole == UMGame.PlayerRole.SPECTATOR)
//                    {
//                        MinecraftForge.EVENT_BUS.post(new SpeedRunnerToSpectatorEvent(serverPlayer, false));
//                    }
                    umPlayer.setBuffedHunter(false); // Player isn't being declared a hunter, so buffs don't apply
                }
                umPlayer.sendUpdateFromServerToSelf(serverPlayer);

                i.getAndIncrement();
            });
        });
        UMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket());
        return i.get();
    }
}
