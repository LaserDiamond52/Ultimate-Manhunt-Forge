package net.laserdiamond.reversemanhunt.network.packet.speedrunner;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.capability.PlayerSpeedRunner;
import net.laserdiamond.reversemanhunt.capability.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.client.ClientPlayer;
import net.laserdiamond.reversemanhunt.client.speedrunner.ClientSpeedRunnerLives;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class SpeedRunnerLifeChangeS2CPacket extends NetworkPacket {

    private final int lives;
    private final boolean wasLastKilledByHunter;

//    public SpeedRunnerLifeChangeS2CPacket(int lives, boolean wasLastKilledByHunter)
//    {
//        this.lives = lives;
//        this.wasLastKilledByHunter = wasLastKilledByHunter;
//    }

    public SpeedRunnerLifeChangeS2CPacket(PlayerSpeedRunner playerSpeedRunner)
    {
        this.lives = playerSpeedRunner.getLives();
        this.wasLastKilledByHunter = playerSpeedRunner.getWasLastKilledByHunter();
    }

    public SpeedRunnerLifeChangeS2CPacket(FriendlyByteBuf buf)
    {
        this.lives = buf.readInt();
        this.wasLastKilledByHunter = buf.readBoolean();

    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.lives);
        buf.writeBoolean(this.wasLastKilledByHunter);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        // ON CLIENT
        ClientSpeedRunnerLives.setLives(this.lives);
        ClientSpeedRunnerLives.setWasLastKilledByHunter(this.wasLastKilledByHunter);
    }
}
