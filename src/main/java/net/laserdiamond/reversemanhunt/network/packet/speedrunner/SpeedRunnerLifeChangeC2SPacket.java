package net.laserdiamond.reversemanhunt.network.packet.speedrunner;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.capability.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class SpeedRunnerLifeChangeC2SPacket extends NetworkPacket {

    private int newLifeCount;
    private boolean wasLastKilledByHunter;

    public SpeedRunnerLifeChangeC2SPacket(int newLifeCount, boolean wasLastKilledByHunter)
    {
        this.newLifeCount = newLifeCount;
        this.wasLastKilledByHunter = wasLastKilledByHunter;
    }

    public SpeedRunnerLifeChangeC2SPacket(FriendlyByteBuf buf) {}

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        final ServerPlayer serverPlayer = context.getSender();
        if (serverPlayer == null)
        {
            return;
        }

        serverPlayer.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
        {
            playerSpeedRunner.setLives(this.newLifeCount);
            playerSpeedRunner.setWasLastKilledByHunter(this.wasLastKilledByHunter);
//            RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(playerSpeedRunner), serverPlayer);
        });
    }
}
