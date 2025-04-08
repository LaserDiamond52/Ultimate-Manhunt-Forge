package net.laserdiamond.ultimatemanhunt.client.hunter;

public class ClientHunterGracePeriod {

    private static int gracePeriodDuration;

    public static void setGracePeriodDuration(int durationTicks)
    {
        ClientHunterGracePeriod.gracePeriodDuration = durationTicks;
    }

    public static int getGracePeriodDuration()
    {
        return ClientHunterGracePeriod.gracePeriodDuration;
    }
}
