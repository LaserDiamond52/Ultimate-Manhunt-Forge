package net.laserdiamond.ultimatemanhunt.api.event;

import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.hunter.HunterReleaseAnnounceS2CPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Event that is called when the Hunters are released after the grace period
 * <p>Event is fired on the {@linkplain net.minecraftforge.common.MinecraftForge#EVENT_BUS main Forge event bus}</p>
 */
public class HuntersReleasedEvent extends Event implements PlayerGameSpawner {

    private final List<Player> hunters;
    private final List<Player> speedRunners;
    private final List<Player> spectators;

    public HuntersReleasedEvent()
    {
        this.hunters = new LinkedList<>();
        this.speedRunners = new LinkedList<>();
        this.spectators = new LinkedList<>();
        UMPlayer.forAllPlayers(
                (player, umPlayer) -> {
                    this.speedRunners.add(player);
                    if (player.level().isClientSide) // Ensure we are on the server
                    {
                        return;
                    }
                    UMPackets.sendToPlayer(new HunterReleaseAnnounceS2CPacket(false), player);
                },
                (player, umPlayer) -> {
                    this.hunters.add(player);
                    if (player.level().isClientSide) // Ensure we are on the server
                    {
                        return;
                    }
                    UMPackets.sendToPlayer(new HunterReleaseAnnounceS2CPacket(true), player);
                    this.spawn(player); // Move hunters to spawn position
                    List<Player> availablePlayerSpeedRunners = UMPlayer.getAvailableSpeedRunners(player);
                    if (!availablePlayerSpeedRunners.isEmpty())
                    {
                        Player trackedPlayer = availablePlayerSpeedRunners.getFirst();
                        umPlayer.setPlayerToTrack(0, trackedPlayer)
                                .sendUpdateFromServerToSelf(player);
                    }
                },
                (player, umPlayer) -> {
                    this.spectators.add(player);
                    if (player.level().isClientSide) // Ensure we are on the server
                    {
                        return;
                    }
                    UMPackets.sendToPlayer(new HunterReleaseAnnounceS2CPacket(false), player);
                },
                (player, umPlayer) -> {}
        );
    }

    public List<Player> getHunters() {
        return new ArrayList<>(this.hunters);
    }

    public List<Player> getSpeedRunners() {
        return new ArrayList<>(this.speedRunners);
    }

    public List<Player> getSpectators() {
        return new ArrayList<>(this.spectators);
    }
}
