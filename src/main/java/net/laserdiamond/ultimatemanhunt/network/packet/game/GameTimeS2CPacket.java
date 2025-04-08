package net.laserdiamond.ultimatemanhunt.network.packet.game;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.client.game.ClientGameTime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class GameTimeS2CPacket extends NetworkPacket {

    private final long gameTime;

    public GameTimeS2CPacket(long gameTime)
    {
        this.gameTime = gameTime;
    }

    public GameTimeS2CPacket(FriendlyByteBuf buf)
    {
        this.gameTime = buf.readLong();

    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeLong(this.gameTime);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        // ON CLIENT
        ClientGameTime.setGameTime(this.gameTime);
    }
}
