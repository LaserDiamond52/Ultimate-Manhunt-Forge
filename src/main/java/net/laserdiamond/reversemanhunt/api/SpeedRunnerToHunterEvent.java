package net.laserdiamond.reversemanhunt.api;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class SpeedRunnerToHunterEvent extends PlayerEvent {

    public SpeedRunnerToHunterEvent(Player player) {
        super(player);
    }


}
