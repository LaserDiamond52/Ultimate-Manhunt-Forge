package net.laserdiamond.ultimatemanhunt.network.packet.speedrunner;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.capability.speedrunner.PlayerSpeedRunner;
import net.laserdiamond.ultimatemanhunt.client.speedrunner.ClientSpeedRunner;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class SpeedRunnerChangeS2CPacket extends NetworkPacket {

    private final int lives;
    private final boolean wasLastKilledByHunter;
    private final long gracePeriodTimeStamp;

    public SpeedRunnerChangeS2CPacket(PlayerSpeedRunner playerSpeedRunner)
    {
        this.lives = playerSpeedRunner.getLives();
        this.wasLastKilledByHunter = playerSpeedRunner.getWasLastKilledByHunter();
        this.gracePeriodTimeStamp = playerSpeedRunner.getGracePeriodTimeStamp();
    }

    public SpeedRunnerChangeS2CPacket(FriendlyByteBuf buf)
    {
        this.lives = buf.readInt();
        this.wasLastKilledByHunter = buf.readBoolean();
        this.gracePeriodTimeStamp = buf.readLong();

    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.lives);
        buf.writeBoolean(this.wasLastKilledByHunter);
        buf.writeLong(this.gracePeriodTimeStamp);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        // ON CLIENT
        ClientSpeedRunner.setLives(this.lives);
        ClientSpeedRunner.setWasLastKilledByHunter(this.wasLastKilledByHunter);
        ClientSpeedRunner.setGracePeriodTimeStamp(this.gracePeriodTimeStamp);
    }
}
