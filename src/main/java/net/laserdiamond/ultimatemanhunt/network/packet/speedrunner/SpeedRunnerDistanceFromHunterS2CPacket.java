package net.laserdiamond.ultimatemanhunt.network.packet.speedrunner;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.client.speedrunner.ClientDistanceFromHunter;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Packet send from the SERVER to the CLIENT that tells speed runners how close they are to the hunter when within the hunter detection range.
 */
public class SpeedRunnerDistanceFromHunterS2CPacket extends NetworkPacket {

    public static void sendNotNearHunterAll(MinecraftServer server)
    {
        server.getPlayerList().getPlayers().forEach(UMSoundEvents::stopDetectionSound);
        UMPackets.sendToAllClients(new SpeedRunnerDistanceFromHunterS2CPacket(-1));
    }

    public static void sendNotNearHunterPlayer(Player player)
    {
        UMSoundEvents.stopDetectionSound(player);
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
    public void packetWork(CustomPayloadEvent.Context context)
    {
        // ON CLIENT
        ClientDistanceFromHunter.setDistance(this.distance);
    }
}
