package net.laserdiamond.reversemanhunt.network.packet.game;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.client.game.ClientRemainingPlayers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class RemainingPlayerCountS2CPacket extends NetworkPacket {

    private final int[] players;

    public RemainingPlayerCountS2CPacket(int speedRunnersRemaining, int huntersRemaining)
    {
        this.players = new int[]{speedRunnersRemaining, huntersRemaining};
    }

    public RemainingPlayerCountS2CPacket(FriendlyByteBuf buf)
    {
        this.players = buf.readVarIntArray();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarIntArray(this.players);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        ClientRemainingPlayers.setRemainingSpeedRunners(this.players[0]);
        ClientRemainingPlayers.setRemainingHunters(this.players[1]);
    }
}
