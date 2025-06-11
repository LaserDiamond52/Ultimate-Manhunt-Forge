package net.laserdiamond.ultimatemanhunt.api.event;

import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

import javax.annotation.Nullable;

/**
 * Event called when a speed runner loses a life
 */
public class SpeedRunnerLifeLossEvent extends PlayerEvent {

    private final boolean wasKilledByHunter;
    private final Player hunter;

    public SpeedRunnerLifeLossEvent(Player speedRunner, Player hunter) {
        super(speedRunner);
        this.hunter = hunter;
        this.wasKilledByHunter = (this.hunter != null);

        if (speedRunner.level().isClientSide)
        {
            return;
        }
        MinecraftServer server = speedRunner.getServer();
        if (server == null)
        {
            return;
        }
        speedRunner.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
        {
            umPlayer.subtractLife()
                    .setWasLastKilledByHunter(wasKilledByHunter)
                    .sendUpdateFromServerToSelf(speedRunner);

            if (this.wasKilledByHunter)
            {
                UMSoundEvents.playFlatlineSound(speedRunner);
                speedRunner.sendSystemMessage(Component.literal(ChatFormatting.RED + "You were killed by a Hunter and lost a life!"));
                UMGame.sendMessageToAllPlayers(server, Component.literal(ChatFormatting.RED + speedRunner.getDisplayName().getString() + " was killed by " + hunter.getDisplayName().getString() + " and lost a life!"));
            } else
            {
                speedRunner.sendSystemMessage(Component.literal(ChatFormatting.RED + "You died and lost a life!"));
                UMGame.sendMessageToAllPlayers(server, Component.literal(ChatFormatting.RED + speedRunner.getDisplayName().getString() + " died and lost a life!"));
            }

            if (umPlayer.getLives() <= 0)
            {
                if (UMGame.getDeadSpeedRunnerRole() == UMGame.PlayerRole.HUNTER)
                {
                    MinecraftForge.EVENT_BUS.post(new SpeedRunnerToHunterEvent(speedRunner, UMPlayer.getIsBuffedHunterOnFinalDeath(), true));
                    UMGame.sendMessageToAllPlayers(server, Component.literal(ChatFormatting.RED + speedRunner.getDisplayName().getString() + " lost all their lives and is now a hunter!"));
                } else
                {
                    MinecraftForge.EVENT_BUS.post(new SpeedRunnerToSpectatorEvent(speedRunner, this.wasKilledByHunter));
                    UMGame.sendMessageToAllPlayers(server, Component.literal(ChatFormatting.RED + speedRunner.getDisplayName().getString() + " lost all their lives is now a spectator!"));
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

    @Nullable
    public Player getHunter()
    {
        return this.hunter;
    }
}
