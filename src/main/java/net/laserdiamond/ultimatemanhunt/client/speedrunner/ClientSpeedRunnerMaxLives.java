package net.laserdiamond.ultimatemanhunt.client.speedrunner;

public class ClientSpeedRunnerMaxLives {

    private static int maxLives;

    public static void setMaxLives(int maxLives)
    {
        ClientSpeedRunnerMaxLives.maxLives = maxLives;
    }

    public static int getMaxLives()
    {
        return ClientSpeedRunnerMaxLives.maxLives;
    }

}
