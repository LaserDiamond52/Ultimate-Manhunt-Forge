package net.laserdiamond.reversemanhunt.capability.client.game;

import net.laserdiamond.reversemanhunt.RMGameState;

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

    public static boolean areHuntersOnGracePeriod()
    {
        return ClientGameTime.gameTime < RMGameState.HUNTER_GRACE_PERIOD_TICKS;
    }
}
