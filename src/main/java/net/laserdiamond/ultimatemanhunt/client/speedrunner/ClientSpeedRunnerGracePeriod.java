package net.laserdiamond.ultimatemanhunt.client.speedrunner;

public class ClientSpeedRunnerGracePeriod {

    private static int gracePeriodDuration;

    public static void setGracePeriodDuration(int durationTicks)
    {
        ClientSpeedRunnerGracePeriod.gracePeriodDuration = durationTicks;
    }

    public static int getGracePeriodDuration()
    {
        return ClientSpeedRunnerGracePeriod.gracePeriodDuration;
    }
}
