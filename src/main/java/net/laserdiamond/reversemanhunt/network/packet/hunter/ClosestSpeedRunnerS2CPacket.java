package net.laserdiamond.reversemanhunt.network.packet.hunter;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.client.hunter.ClientSpeedRunnerDistance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.UUID;

/**
 * Packet sent from the SERVER to the CLIENT that helps hunters find the closest speed runner to them
 */
public class ClosestSpeedRunnerS2CPacket extends NetworkPacket {

    private final boolean speedRunnersPresent;
    private final int entityId;
    private final float distance;

    public ClosestSpeedRunnerS2CPacket(boolean speedRunnersPresent, int entityId, float distance)
    {
        this.speedRunnersPresent = speedRunnersPresent;
        this.entityId = entityId;
        this.distance = distance;
    }

    public ClosestSpeedRunnerS2CPacket(FriendlyByteBuf buf)
    {
        this.speedRunnersPresent = buf.readBoolean();
        this.entityId = buf.readInt();
        this.distance = buf.readFloat();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.speedRunnersPresent);
        buf.writeInt(this.entityId);
        buf.writeFloat(this.distance);
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
        ClientLevel cl = player.clientLevel;
        Entity trackedEntity = cl.getEntity(this.entityId);
        if (trackedEntity instanceof Player trackedPlayer)
        {
            ClientSpeedRunnerDistance.setTrackedPlayer(trackedPlayer);
        }
        ClientSpeedRunnerDistance.setSpeedRunnersPresent(this.speedRunnersPresent);
        ClientSpeedRunnerDistance.setDistance(this.distance);

    }
}
