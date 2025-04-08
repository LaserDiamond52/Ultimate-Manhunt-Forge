package net.laserdiamond.ultimatemanhunt.network.packet.game;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.client.game.ClientHardcore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class HardcoreUpdateS2CPacket extends NetworkPacket {

    private final boolean isHardcore;

    public HardcoreUpdateS2CPacket(boolean isHardcore)
    {
        this.isHardcore = isHardcore;
    }

    public HardcoreUpdateS2CPacket(FriendlyByteBuf buf)
    {
        this.isHardcore = buf.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isHardcore);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        ClientHardcore.setHardcore(this.isHardcore);
    }
}
