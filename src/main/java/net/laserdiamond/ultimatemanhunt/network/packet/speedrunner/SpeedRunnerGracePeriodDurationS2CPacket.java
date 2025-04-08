package net.laserdiamond.ultimatemanhunt.network.packet.speedrunner;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.client.speedrunner.ClientSpeedRunnerGracePeriod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class SpeedRunnerGracePeriodDurationS2CPacket extends NetworkPacket {

    private final int durationTicks;

    public SpeedRunnerGracePeriodDurationS2CPacket(int durationTicks)
    {
        this.durationTicks = durationTicks;
    }

    public SpeedRunnerGracePeriodDurationS2CPacket(FriendlyByteBuf buf)
    {
        this.durationTicks = buf.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.durationTicks);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        ClientSpeedRunnerGracePeriod.setGracePeriodDuration(this.durationTicks);
    }
}
