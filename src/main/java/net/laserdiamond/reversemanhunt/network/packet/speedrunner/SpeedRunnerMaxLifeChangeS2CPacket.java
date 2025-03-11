package net.laserdiamond.reversemanhunt.network.packet.speedrunner;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.client.speedrunner.ClientSpeedRunner;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

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
    public void packetWork(CustomPayloadEvent.Context context)
    {
        ClientSpeedRunner.setMaxLives(this.maxLives);
    }
}
