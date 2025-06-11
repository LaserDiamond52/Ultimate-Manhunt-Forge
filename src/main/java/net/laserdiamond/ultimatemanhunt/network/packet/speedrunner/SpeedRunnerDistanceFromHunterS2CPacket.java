package net.laserdiamond.ultimatemanhunt.network.packet.speedrunner;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.client.speedrunner.ClientDistanceFromHunter;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/**
 * Packet send from the SERVER to the CLIENT that tells speed runners how close they are to a hunter when within the hunter detection range.
 */
public class SpeedRunnerDistanceFromHunterS2CPacket extends NetworkPacket {

    public static void sendNotNearHunterAll()
    {
        UMPackets.sendToAllClients(new SpeedRunnerDistanceFromHunterS2CPacket(-1));
    }

    public static void sendNotNearHunterPlayer(Player player)
    {
        UMPackets.sendToPlayer(new SpeedRunnerDistanceFromHunterS2CPacket(-1), player);
    }

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
    public void packetWork(NetworkEvent.Context context)
    {
        // ON CLIENT
        ClientDistanceFromHunter.setDistance(this.distance);
    }
}
