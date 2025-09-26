package net.laserdiamond.ultimatemanhunt.network.packet.game;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.client.game.ClientRemainingPlayers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedList;

public class RemainingPlayerCountS2CPacket extends NetworkPacket {

    private final int[] players;

    public RemainingPlayerCountS2CPacket()
    {
        LinkedList<Player> speedRunners = new LinkedList<>();
        LinkedList<Player> hunters = new LinkedList<>();
        UMPlayer.forAllPlayers(
                (player, umPlayer) -> speedRunners.add(player),
                (player, umPlayer) -> hunters.add(player),
                (player, umPlayer) -> {},
                (player, umPlayer) -> {}
        );
        this.players = new int[]{speedRunners.size(), hunters.size()};
    }

    public RemainingPlayerCountS2CPacket(Player excludedPlayer)
    {
        LinkedList<Player> speedRunners = new LinkedList<>();
        LinkedList<Player> hunters = new LinkedList<>();
        UMPlayer.forAllPlayers(
                (player, umPlayer) -> {
                    if (player.getUUID() == excludedPlayer.getUUID())
                    {
                        return;
                    }
                    speedRunners.add(player);
                },
                (player, umPlayer) -> {
                    if (player.getUUID() == excludedPlayer.getUUID())
                    {
                        return;
                    }
                    hunters.add(player);
                },
                (player, umPlayer) -> {},
                (player, umPlayer) -> {}
        );
        this.players = new int[]{speedRunners.size(), hunters.size()};
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
    public void packetWork(NetworkEvent.Context context)
    {
        ClientRemainingPlayers.setRemainingSpeedRunners(this.players[0]);
        ClientRemainingPlayers.setRemainingHunters(this.players[1]);
    }
}
