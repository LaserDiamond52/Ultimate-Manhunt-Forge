package net.laserdiamond.reversemanhunt.capability.client.hunter;

import java.util.UUID;

public class ClientSpeedRunnerDistance {

    private static boolean speedRunnersPresent;
    private static UUID playerUUID;
    private static float distance;

    public static void setSpeedRunnersPresent(boolean speedRunnersPresent)
    {
        ClientSpeedRunnerDistance.speedRunnersPresent = speedRunnersPresent;
    }

    public static boolean areSpeedRunnersPresent()
    {
        return ClientSpeedRunnerDistance.speedRunnersPresent;
    }

    public static void setPlayerUUID(UUID playerUUID)
    {
        ClientSpeedRunnerDistance.playerUUID = playerUUID;
    }

    public static UUID getPlayerUUID()
    {
        return ClientSpeedRunnerDistance.playerUUID;
    }

    public static void setDistance(float distance)
    {
        ClientSpeedRunnerDistance.distance = distance;
    }

    public static float getDistance()
    {
        return ClientSpeedRunnerDistance.distance;
    }
}
