package net.laserdiamond.reversemanhunt.network.packet.hunter;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.client.hunter.ClientSpeedRunnerDistance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.UUID;

/**
 * Packet sent from the SERVER to the CLIENT that helps hunters find the closest speed runner to them
 */
public class ClosestSpeedRunnerS2CPacket extends NetworkPacket {

    private final boolean speedRunnersPresent;
    private final UUID playerUUID;
    private final float distance;

    public ClosestSpeedRunnerS2CPacket(boolean speedRunnersPresent, UUID playerUUID, float distance)
    {
        this.speedRunnersPresent = speedRunnersPresent;
        this.playerUUID = playerUUID;
        this.distance = distance;
    }

    public ClosestSpeedRunnerS2CPacket(FriendlyByteBuf buf)
    {
        this.speedRunnersPresent = buf.readBoolean();
        this.playerUUID = buf.readUUID();
        this.distance = buf.readFloat();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.speedRunnersPresent);
        buf.writeUUID(this.playerUUID);
        buf.writeFloat(this.distance);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        // ON CLIENT
        ClientSpeedRunnerDistance.setSpeedRunnersPresent(this.speedRunnersPresent);
        ClientSpeedRunnerDistance.setPlayerUUID(this.playerUUID);
        ClientSpeedRunnerDistance.setDistance(this.distance);
    }
}
