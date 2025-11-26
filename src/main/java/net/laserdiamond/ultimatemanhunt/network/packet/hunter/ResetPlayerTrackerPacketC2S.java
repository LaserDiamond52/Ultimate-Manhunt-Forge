package net.laserdiamond.ultimatemanhunt.network.packet.hunter;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;

public class ResetPlayerTrackerPacketC2S extends NetworkPacket {

    public ResetPlayerTrackerPacketC2S() {}

    public ResetPlayerTrackerPacketC2S(FriendlyByteBuf buf) {}

    @Override
    public void packetWork(NetworkEvent.Context context)
    {
        ServerPlayer player = context.getSender();
        if (player == null)
        {
            return;
        }

        player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
        {
            if (umPlayer.isHunter() && UMGame.State.isGameRunning())
            {
                long gameTime = UMGame.getCurrentGameTime();
                if (!umPlayer.isTrackerCooldownDone(gameTime))
                {
                    player.sendSystemMessage(Component.literal("Tracker reset is on cooldown. Try again in a moment"));
                    return;
                }
                List<Player> trackablePlayers = UMPlayer.getAvailableSpeedRunners(player);
                umPlayer.resetTrackerCooldown(gameTime);
                if (!trackablePlayers.isEmpty())
                {
                    umPlayer.setPlayerToTrack(0, trackablePlayers.get(0));
                    player.sendSystemMessage(Component.literal("Tracker reset!"));
                } else
                {
                    player.sendSystemMessage(Component.literal("Tracker reset! There are no players to track right now"));
                }
            }
        });
    }
}
