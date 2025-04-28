package net.laserdiamond.ultimatemanhunt.network.packet.hunter;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.laserdiamond.ultimatemanhunt.client.UMKeyBindings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Packet sent from the CLIENT to the SERVER when a hunter wants to change the {@linkplain Player player} they are tracking
 */
public class ChangeTrackingSpeedRunnerC2SPacket extends NetworkPacket {

    private final boolean isNext;

    public ChangeTrackingSpeedRunnerC2SPacket(@NotNull UMKeyBindings.TrackCycleDirection direction)
    {
        this.isNext = switch (direction)
        {
            case NEXT -> true;
            case PREVIOUS -> false;
        };
    }

    public ChangeTrackingSpeedRunnerC2SPacket(FriendlyByteBuf buf)
    {
        this.isNext = buf.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBoolean(this.isNext);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        final ServerPlayer player = context.getSender();
        if (player == null)
        {
            return;
        }
        List<Player> availableSpeedRunners = UMPlayer.getAvailableSpeedRunners(player);

        player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
        {
            int trackingIndex = umPlayer.getTrackingIndex();
            if (availableSpeedRunners.isEmpty())
            {
                return;
            }
            Player targetPlayer;
            if (this.isNext)
            {
                trackingIndex = incrementTrackingIndex(trackingIndex, availableSpeedRunners.size());
            } else
            {
                trackingIndex = decrementTrackingIndex(trackingIndex, availableSpeedRunners.size());
            }
            targetPlayer = availableSpeedRunners.get(trackingIndex);
            umPlayer.setPlayerToTrack(trackingIndex, targetPlayer)
                        .sendUpdateFromServerToSelf(player);
        });

//        player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
//        {
//            int trackingIndex = playerHunter.getTrackingIndex();
//            if (availableSpeedRunners.isEmpty())
//            {
//                return;
//            }
//            Player targetPlayer;
//            if (this.isNext)
//            {
//                trackingIndex = incrementTrackingIndex(trackingIndex, availableSpeedRunners.size());
//            } else
//            {
//                trackingIndex = decrementTrackingIndex(trackingIndex, availableSpeedRunners.size());
//            }
//            targetPlayer = availableSpeedRunners.get(trackingIndex);
//            playerHunter.setPlayerToTrack(trackingIndex, targetPlayer);
//
//            UMPackets.sendToAllTrackingEntityAndSelf(new HunterCapabilitySyncS2CPacket(player, playerHunter), player);
//        });

    }

    private int incrementTrackingIndex(int trackingIndex, int max)
    {
        trackingIndex++; // Increment by 1
        if (trackingIndex > max) // Over max?
        {
            return 0; // Return 0
        }
        return trackingIndex;
    }

    private int decrementTrackingIndex(int trackingIndex, int max)
    {
        trackingIndex--; // Decrement by 1
        if (trackingIndex < 0) // Less than 0?
        {
            return max; // Return max
        }
        return trackingIndex;
    }
}
