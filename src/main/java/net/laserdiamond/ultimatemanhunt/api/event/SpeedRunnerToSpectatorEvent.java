package net.laserdiamond.ultimatemanhunt.api.event;

import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.game.RemainingPlayerCountS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Event called when a Speed Runner loses all their lives and becomes a spectator
 */
public class SpeedRunnerToSpectatorEvent extends PlayerEvent {

    private final boolean wasKilled;

    public SpeedRunnerToSpectatorEvent(Player player, boolean wasKilled) {
        super(player);
        this.wasKilled = wasKilled;
        if (player.level().isClientSide)
        {
            return;
        }
        player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
        {
            umPlayer.resetToSpectator(player, false);
            if (this.wasKilled)
            {
                player.sendSystemMessage(Component.literal(ChatFormatting.DARK_RED + "You have lost all your lives and are now a Spectator!"));
            }

            UMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket());
        });
    }
}
