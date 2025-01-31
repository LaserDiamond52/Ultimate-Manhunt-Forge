package net.laserdiamond.reversemanhunt.event;

import net.laserdiamond.reversemanhunt.capability.PlayerHunter;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Event that is called when the Hunters are released after the grace period
 * <p>Event is fired on the {@linkplain net.minecraftforge.common.MinecraftForge#EVENT_BUS main Forge event bus}</p>
 */
public class HuntersReleasedEvent extends Event {

    private final List<Player> hunters;

    public HuntersReleasedEvent(List<Player> hunters)
    {
        this.hunters = hunters;
        for (Player player : this.hunters)
        {
            player.getAttributes().removeAttributeModifiers(PlayerHunter.createHunterSpawnAttributes()); // Remove spawn attributes from hunter
        }
    }

    public List<Player> getHunters() {
        return hunters;
    }
}
