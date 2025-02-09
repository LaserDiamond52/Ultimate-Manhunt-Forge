package net.laserdiamond.reversemanhunt.network.packet.speedrunner;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.client.speedrunner.ClientSpeedRunnerHunterDetection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class HunterDetectionS2CPacket extends NetworkPacket {

    private final boolean isNear;

    public HunterDetectionS2CPacket(boolean isNear) {
        this.isNear = isNear;
    }

    public HunterDetectionS2CPacket(FriendlyByteBuf buf)
    {
        this.isNear = buf.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isNear);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        // ON CLIENT
        ClientSpeedRunnerHunterDetection.setIsNearHunter(this.isNear);
    }
}
