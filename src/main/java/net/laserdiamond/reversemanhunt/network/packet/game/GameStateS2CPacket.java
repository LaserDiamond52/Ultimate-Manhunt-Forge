package net.laserdiamond.reversemanhunt.network.packet.game;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.RMGame;
import net.laserdiamond.reversemanhunt.client.game.ClientGameState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * {@linkplain NetworkPacket Packet} sent from the server to the client to indicate the current {@link RMGame.State game state}
 */
public class GameStateS2CPacket extends NetworkPacket {

    private final RMGame.State gameState;

    public GameStateS2CPacket(RMGame.State gameState)
    {
        this.gameState = gameState;
    }

    public GameStateS2CPacket(FriendlyByteBuf buf)
    {
        this.gameState = RMGame.State.fromOrdinal(buf.readInt());
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
