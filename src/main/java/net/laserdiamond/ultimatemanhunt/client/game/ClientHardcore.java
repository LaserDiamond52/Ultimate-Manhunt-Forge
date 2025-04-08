package net.laserdiamond.ultimatemanhunt.client.game;

public class ClientHardcore {

    private static boolean isHardcore;

    public static void setHardcore(boolean isHardcore)
    {
        ClientHardcore.isHardcore = isHardcore;
    }

    public static boolean isHardcore()
    {
        return ClientHardcore.isHardcore;
    }
}
