package net.laserdiamond.ultimatemanhunt.network.packet.speedrunner;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.client.speedrunner.ClientDistanceFromHunter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Packet send from the SERVER to the CLIENT that tells speed runners how close they are to the hunter when within the hunter detection range.
 */
public class SpeedRunnerDistanceFromHunterS2CPacket extends NetworkPacket {

    private final float distance;

    public SpeedRunnerDistanceFromHunterS2CPacket(float distance)
    {
        this.distance = distance;
    }

    public SpeedRunnerDistanceFromHunterS2CPacket(FriendlyByteBuf buf)
    {
        this.distance = buf.readFloat();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(this.distance);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        // ON CLIENT
        ClientDistanceFromHunter.setDistance(this.distance);
    }
}
