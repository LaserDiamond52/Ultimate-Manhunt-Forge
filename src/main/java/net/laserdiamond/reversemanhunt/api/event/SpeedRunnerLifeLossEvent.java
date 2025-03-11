package net.laserdiamond.reversemanhunt.api.event;

import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerCapabilitySyncS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerChangeS2CPacket;
import net.laserdiamond.reversemanhunt.sound.RMSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Event called when a speed runner loses a life
 */
public class SpeedRunnerLifeLossEvent extends PlayerEvent {

    private final boolean wasKilledByHunter;

    public SpeedRunnerLifeLossEvent(Player player, boolean wasKilledByHunter) {
        super(player);
        this.wasKilledByHunter = wasKilledByHunter;
        player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
        {
            playerSpeedRunner.subtractLife();
            playerSpeedRunner.setWasLastKilledByHunter(this.wasKilledByHunter);
            RMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
            RMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player.getId(), playerSpeedRunner.toNBT()), player);

            RMSoundEvents.playFlatlineSound(player);
            if (this.wasKilledByHunter)
            {
                player.sendSystemMessage(Component.literal(ChatFormatting.RED + "You were killed by a Hunter and lost a life!"));
            } else
            {
                player.sendSystemMessage(Component.literal(ChatFormatting.RED + "You died and lost a life!"));
            }
        });
    }

    /**
     * Returns whether the life loss was due to a hunter
     * @return True if the life loss was from a hunter, false otherwise
     */
    public boolean isWasKilledByHunter() {
        return this.wasKilledByHunter;
    }
}
