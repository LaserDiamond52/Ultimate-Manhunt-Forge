package net.laserdiamond.reversemanhunt.client;

import net.minecraft.client.player.LocalPlayer;

public class ClientPlayer {

    private static LocalPlayer player;

    public static void setPlayer(LocalPlayer player)
    {
        ClientPlayer.player = player;
    }

    public static LocalPlayer getPlayer()
    {
        return ClientPlayer.player;
    }
}
