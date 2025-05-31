package net.laserdiamond.ultimatemanhunt.network.packet.game;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.client.game.ClientGameState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * {@linkplain NetworkPacket Packet} sent from the server to the client to indicate the current {@link UMGame.State game state}
 */
public class GameStateS2CPacket extends NetworkPacket {

    private final UMGame.State gameState;

    public GameStateS2CPacket(UMGame.State gameState)
    {
        this.gameState = gameState;
    }

    public GameStateS2CPacket(FriendlyByteBuf buf)
    {
        this.gameState = UMGame.State.fromOrdinal(buf.readInt());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeInt(this.gameState.ordinal());
    }

    @Override
    public void packetWork(NetworkEvent.Context context)
    {
        // ON CLIENT
        ClientGameState.setGameState(this.gameState);
    }
}
