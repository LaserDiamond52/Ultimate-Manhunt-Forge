package net.laserdiamond.ultimatemanhunt.api.event;

import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Event called when a speed runner loses a life
 */
public class SpeedRunnerLifeLossEvent extends PlayerEvent {

    private final boolean wasKilledByHunter;

    public SpeedRunnerLifeLossEvent(Player player, boolean wasKilledByHunter) {
        super(player);
        this.wasKilledByHunter = wasKilledByHunter;

        player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
        {
            umPlayer.subtractLife()
                    .setWasLastKilledByHunter(this.wasKilledByHunter)
                    .sendUpdateFromServerToSelf(player);

            UMSoundEvents.playFlatlineSound(player);
            if (this.wasKilledByHunter)
            {
                player.sendSystemMessage(Component.literal(ChatFormatting.RED + "You were killed by a Hunter and lost a life!"));
            } else
            {
                player.sendSystemMessage(Component.literal(ChatFormatting.RED + "You died and lost a life!"));
            }

            if (umPlayer.getLives() <= 0)
            {
                if (UMGame.getDeadSpeedRunnerRole() == UMGame.PlayerRole.HUNTER)
                {
                    MinecraftForge.EVENT_BUS.post(new SpeedRunnerToHunterEvent(player, UMPlayer.getIsBuffedHunterOnFinalDeath(), true));
                } else
                {
                    umPlayer.resetToSpectator(player, false);
                }

                if (UMPlayer.getRemainingSpeedRunners().isEmpty()) // Check if there are any remaining speed runners
                {
                    MinecraftForge.EVENT_BUS.post(new UltimateManhuntGameStateEvent.End(UltimateManhuntGameStateEvent.End.Reason.HUNTER_WIN)); // No more speed runners. Hunters win!
                }
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
