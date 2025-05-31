package net.laserdiamond.ultimatemanhunt.network.packet.hunter;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.client.hunter.ClientHunterGracePeriod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class HunterGracePeriodDurationS2CPacket extends NetworkPacket {

    private final int durationTicks;

    public HunterGracePeriodDurationS2CPacket(int durationTicks)
    {
        this.durationTicks = durationTicks;
    }

    public HunterGracePeriodDurationS2CPacket(FriendlyByteBuf buf)
    {
        this.durationTicks = buf.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.durationTicks);
    }

    @Override
    public void packetWork(NetworkEvent.Context context)
    {
        ClientHunterGracePeriod.setGracePeriodDuration(this.durationTicks);
    }
}
