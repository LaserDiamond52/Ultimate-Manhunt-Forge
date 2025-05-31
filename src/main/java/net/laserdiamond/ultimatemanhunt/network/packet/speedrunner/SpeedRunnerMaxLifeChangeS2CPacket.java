package net.laserdiamond.ultimatemanhunt.network.packet.speedrunner;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.client.speedrunner.ClientSpeedRunnerMaxLives;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SpeedRunnerMaxLifeChangeS2CPacket extends NetworkPacket {

    private final int maxLives;

    public SpeedRunnerMaxLifeChangeS2CPacket(int maxLives)
    {
        this.maxLives = maxLives;
    }

    public SpeedRunnerMaxLifeChangeS2CPacket(FriendlyByteBuf buf)
    {
        this.maxLives = buf.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.maxLives);
    }

    @Override
    public void packetWork(NetworkEvent.Context context)
    {
        ClientSpeedRunnerMaxLives.setMaxLives(this.maxLives);
    }
}
