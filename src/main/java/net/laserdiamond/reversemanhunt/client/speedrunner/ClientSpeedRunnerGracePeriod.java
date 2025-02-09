package net.laserdiamond.reversemanhunt.client.speedrunner;

import net.laserdiamond.reversemanhunt.client.hunter.ClientHunterGracePeriod;

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
