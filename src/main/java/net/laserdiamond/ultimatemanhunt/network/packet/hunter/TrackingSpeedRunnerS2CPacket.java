package net.laserdiamond.ultimatemanhunt.network.packet.hunter;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.client.hunter.ClientTrackedSpeedRunner;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.UUID;

/**
 * Packet sent from the SERVER to the CLIENT that updates the tracking position of a speed runner for a hunter
 */
public class TrackingSpeedRunnerS2CPacket extends NetworkPacket {

    public static void sendNonTracking(Player player)
    {
        UMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(false, player, 0F), player);
    }

    private final boolean speedRunnersPresent;
    private final String playerName;
    private final UUID playerUUID;
    private final float distance;
    private final Vec3 position;

    public TrackingSpeedRunnerS2CPacket(boolean speedRunnersPresent, Player player, float distance)
    {
        this.speedRunnersPresent = speedRunnersPresent;
        this.playerName = player.getName().getString();
        this.playerUUID = player.getUUID();
        this.distance = distance;
        this.position = player.getEyePosition();
    }

    public TrackingSpeedRunnerS2CPacket(FriendlyByteBuf buf)
    {
        this.speedRunnersPresent = buf.readBoolean();
        this.playerName = buf.readUtf();
        this.playerUUID = buf.readUUID();
        this.distance = buf.readFloat();
        this.position = buf.readVec3();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.speedRunnersPresent);
        buf.writeUtf(this.playerName);
        buf.writeUUID(this.playerUUID);
        buf.writeFloat(this.distance);
        buf.writeVec3(this.position);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        // ON CLIENT
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
        {
            return;
        }
        ClientTrackedSpeedRunner.setSpeedRunnersPresent(this.speedRunnersPresent);
        ClientTrackedSpeedRunner.setTrackedPlayerName(this.playerName);
        ClientTrackedSpeedRunner.setTrackedPlayerUUID(this.playerUUID);
        ClientTrackedSpeedRunner.setDistance(this.distance);
        ClientTrackedSpeedRunner.setPosition(this.position);

    }
}
