package net.laserdiamond.reversemanhunt.api.event;

import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterCapabilitySyncS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterReleaseAnnounceS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.TrackingSpeedRunnerS2CPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Event that is called when the Hunters are released after the grace period
 * <p>Event is fired on the {@linkplain net.minecraftforge.common.MinecraftForge#EVENT_BUS main Forge event bus}</p>
 */
public class HuntersReleasedEvent extends Event implements PlayerGameSpawner {

    private final List<Player> hunters;
    private final List<Player> speedRunners;

    public HuntersReleasedEvent(List<Player> hunters, List<Player> speedRunners)
    {
        this.hunters = hunters;
        this.speedRunners = speedRunners;
        for (Player player : this.hunters)
        {
            if (player.level().isClientSide) // Ensure we are on the server
            {
                return;
            }
            player.getAttributes().removeAttributeModifiers(PlayerHunter.createHunterSpawnAttributes()); // Remove spawn attributes from hunter
            RMPackets.sendToPlayer(new HunterReleaseAnnounceS2CPacket(true), player);
            this.spawn(player); // Move hunters to spawn position
            player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
            {
                List<Player> availablePlayerSpeedRunners = PlayerHunter.getAvailableSpeedRunners(player);
                if (!availablePlayerSpeedRunners.isEmpty()) // Are there any speed runners to start tracking?
                {
                    Player trackedPlayer = availablePlayerSpeedRunners.getFirst();
                    playerHunter.setPlayerToTrack(0, trackedPlayer); // Default all hunters to track this player
                    RMPackets.sendToAllTrackingEntityAndSelf(new HunterCapabilitySyncS2CPacket(player.getId(), playerHunter.toNBT()), player);
                }
            });
        }
        for (Player player : this.speedRunners)
        {
            if (player.level().isClientSide) // Ensure we are on the server
            {
                return;
            }
            RMPackets.sendToPlayer(new HunterReleaseAnnounceS2CPacket(false), player);
        }
    }

    public List<Player> getHunters() {
        return hunters;
    }

    public List<Player> getSpeedRunners() {
        return speedRunners;
    }
}
