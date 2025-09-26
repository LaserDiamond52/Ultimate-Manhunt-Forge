package net.laserdiamond.ultimatemanhunt.capability;

import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.hunter.TrackingSpeedRunnerS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID)
public class HunterPlayerTracker {

    @SubscribeEvent
    public static void onPlayerServerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START || event.side == LogicalSide.CLIENT || !UMGame.State.isGameRunning())
        {
            return;
        }
        Player player = event.player;
        Level level = player.level();
        if (level.isClientSide)
        {
            return;
        }
        MinecraftServer mcServer = player.getServer();
        if (mcServer == null || mcServer.getTickCount() % 5 != 0) // Update every 5 ticks! (Any faster could cause server lag)
        {
            return;
        }
        player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
        {
            if (umPlayer.isHunter())
            {
                List<Player> availableTrackingTargets = UMPlayer.getAvailableSpeedRunners(player);
                if (availableTrackingTargets.isEmpty())
                {
                    TrackingSpeedRunnerS2CPacket.sendNonTracking(player);
                    return;
                }
//                for (Player speedRunnerPlayer : availableTrackingTargets)
//                {
//                    if (UMPlayer.isSpeedRunnerOnGracePeriodServer(speedRunnerPlayer))
//                    {
//                        // Set to track first available player
//                        continue;
//                    }
//
//                }

                UUID trackedPlayerUUID = umPlayer.getTrackingPlayerUUID(); // The player the hunter is targeting
                if (trackedPlayerUUID == player.getUUID())
                {
                    TrackingSpeedRunnerS2CPacket.sendNonTracking(player); // Attempting to track themselves. End method
                    return;
                }
                Player trackedPlayer = mcServer.getPlayerList().getPlayer(trackedPlayerUUID);
                if (trackedPlayer == null)
                {
                    TrackingSpeedRunnerS2CPacket.sendNonTracking(player); // Player doesn't exist. End method
                    return;
                }
                if (!trackedPlayer.level().isClientSide)
                {
                    if (!trackedPlayer.level().dimension().equals(level.dimension()) || UMPlayer.isSpeedRunnerOnGracePeriodServer(trackedPlayer) || !trackedPlayer.isAlive())
                    {
                        TrackingSpeedRunnerS2CPacket.sendNonTracking(player); // Player is either not in the same dimension, on grace period, or dead. End method
                        return;
                    }
                    LazyOptional<UMPlayer> trackedPlayerCap = trackedPlayer.getCapability(UMPlayerCapability.UM_PLAYER); // Get hunter capability of tracked player
                    if (trackedPlayerCap.isPresent()) // Is the capability present?
                    {
                        UMPlayer trackedPlayerHunter = trackedPlayerCap.orElse(new UMPlayer(trackedPlayerUUID));
                        if (!trackedPlayerHunter.isSpeedRunner()) // Is the tracked player NOT a speed runner (Player could change roles while being tracked)
                        {
                            TrackingSpeedRunnerS2CPacket.sendNonTracking(player); // Player is a hunter. Do not track, and end method
                            return;
                        }
                    }
                }
                UMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(true, trackedPlayer), player); // Hunter is now tracking this player. Send information
            }
        });
    }
}
