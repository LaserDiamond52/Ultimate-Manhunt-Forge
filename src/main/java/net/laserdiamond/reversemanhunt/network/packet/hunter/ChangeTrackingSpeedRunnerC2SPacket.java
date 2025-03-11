package net.laserdiamond.reversemanhunt.network.packet.hunter;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.client.RMKeyBindings;
import net.laserdiamond.reversemanhunt.network.RMPackets;
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

    public ChangeTrackingSpeedRunnerC2SPacket(@NotNull RMKeyBindings.TrackCycleDirection direction)
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
        List<Player> availableSpeedRunners = PlayerHunter.getAvailableSpeedRunners(player);

        player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
        {
            int trackingIndex = playerHunter.getTrackingIndex();
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
            playerHunter.setPlayerToTrack(trackingIndex, targetPlayer);

            RMPackets.sendToAllTrackingEntityAndSelf(new HunterCapabilitySyncS2CPacket(player.getId(), playerHunter.toNBT()), player);
        });

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
