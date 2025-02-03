package net.laserdiamond.reversemanhunt.event;

import net.laserdiamond.reversemanhunt.capability.PlayerHunter;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterReleaseAnnounceS2CPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Event that is called when the Hunters are released after the grace period
 * <p>Event is fired on the {@linkplain net.minecraftforge.common.MinecraftForge#EVENT_BUS main Forge event bus}</p>
 */
public class HuntersReleasedEvent extends Event {

    private final List<Player> hunters;
    private final List<Player> speedRunners;

    public HuntersReleasedEvent(List<Player> hunters, List<Player> speedRunners)
    {
        this.hunters = hunters;
        this.speedRunners = speedRunners;
        for (Player player : this.hunters)
        {
            player.getAttributes().removeAttributeModifiers(PlayerHunter.createHunterSpawnAttributes()); // Remove spawn attributes from hunter
            RMPackets.sendToPlayer(new HunterReleaseAnnounceS2CPacket(true), player);
        }
        for (Player player : this.speedRunners)
        {
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
