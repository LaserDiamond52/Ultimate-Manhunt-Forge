package net.laserdiamond.reversemanhunt.client.game;

/**
 * Game time on the CLIENT
 */
public class ClientGameTime {

    private static long gameTime;

    public static void setGameTime(long gameTime)
    {
        ClientGameTime.gameTime = Math.max(0, gameTime);
    }

    public static long getGameTime()
    {
        return ClientGameTime.gameTime;
    }
}
