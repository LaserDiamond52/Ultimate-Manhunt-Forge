package net.laserdiamond.reversemanhunt.network.packet.game;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.RMGameState;
import net.laserdiamond.reversemanhunt.capability.client.game.ClientGameState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * {@linkplain NetworkPacket Packet} sent from the server to the client to indicate the current {@link RMGameState.State game state}
 */
public class GameStateS2CPacket extends NetworkPacket {

    private final RMGameState.State gameState;

    public GameStateS2CPacket(RMGameState.State gameState)
    {
        this.gameState = gameState;
    }

    public GameStateS2CPacket(FriendlyByteBuf buf)
    {
        this.gameState = RMGameState.State.fromOrdinal(buf.readInt());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeInt(this.gameState.ordinal());
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        // ON CLIENT
        ClientGameState.setGameState(this.gameState);
    }
}
