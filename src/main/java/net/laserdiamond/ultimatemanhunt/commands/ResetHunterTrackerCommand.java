package net.laserdiamond.ultimatemanhunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ResetHunterTrackerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("resetPlayerTracker")
                        .requires(ResetHunterTrackerCommand::isEligible)
                        .executes(context -> ResetHunterTrackerCommand.resetTracker(context.getSource()))
        );
    }

    private static boolean isEligible(CommandSourceStack sourceStack)
    {
        AtomicBoolean ret = new AtomicBoolean(false);
        if (sourceStack.getEntity() instanceof Player player)
        {
            player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                if (umPlayer.isHunter() && UMGame.State.isGameRunning())
                {
                    ret.set(true);
                }
            });
        }
        return ret.get();
    }

    private static int resetTracker(CommandSourceStack sourceStack)
    {
        AtomicInteger ret = new AtomicInteger(0);

        if (sourceStack.getEntity() instanceof Player player)
        {
            player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                if (umPlayer.isHunter() && UMGame.State.isGameRunning())
                {
                    List<Player> trackablePlayers = UMPlayer.getAvailableSpeedRunners(player);
                    umPlayer.setPlayerToTrack(0, trackablePlayers.get(0));
                    ret.getAndIncrement();
                    player.sendSystemMessage(Component.literal("Tracker reset!"));
                }
            });
        }
        return ret.get();
    }

}
