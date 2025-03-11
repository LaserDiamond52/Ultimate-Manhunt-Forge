package net.laserdiamond.reversemanhunt.client.speedrunner;

import net.laserdiamond.reversemanhunt.RMGame;

public class ClientSpeedRunner {

    private static int lives;
    private static int maxLives;
    private static boolean wasLastKilledByHunter;
    private static long gracePeriodTimeStamp;

    public static void setLives(int lives)
    {
        ClientSpeedRunner.lives = Math.max(0, Math.min(lives, ClientSpeedRunner.getMaxLives()));
    }

    public static int getLives()
    {
        return ClientSpeedRunner.lives;
    }

    public static void setWasLastKilledByHunter(boolean wasLastKilledByHunter)
    {
        ClientSpeedRunner.wasLastKilledByHunter = wasLastKilledByHunter;
    }

    public static boolean getWasLastKilledByHunter()
    {
        return ClientSpeedRunner.wasLastKilledByHunter;
    }

    public static void setMaxLives(int maxLives)
    {
        ClientSpeedRunner.maxLives = maxLives;
    }

    public static int getMaxLives()
    {
        return ClientSpeedRunner.maxLives;
    }

    public static void setGracePeriodTimeStamp(long gracePeriodTimeStamp)
    {
        ClientSpeedRunner.gracePeriodTimeStamp = gracePeriodTimeStamp;
    }

    public static long getGracePeriodTimeStamp()
    {
        return ClientSpeedRunner.gracePeriodTimeStamp;
    }
}
