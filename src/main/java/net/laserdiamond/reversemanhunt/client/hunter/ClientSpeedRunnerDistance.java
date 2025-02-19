package net.laserdiamond.reversemanhunt.client.hunter;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ClientSpeedRunnerDistance {

    private static boolean speedRunnersPresent;
    private static Player player;
    private static float distance;

    public static void setSpeedRunnersPresent(boolean speedRunnersPresent)
    {
        ClientSpeedRunnerDistance.speedRunnersPresent = speedRunnersPresent;
    }

    public static boolean areSpeedRunnersPresent()
    {
        return ClientSpeedRunnerDistance.speedRunnersPresent;
    }

    public static void setTrackedPlayer(Player player)
    {
        ClientSpeedRunnerDistance.player = player;
    }

    public static Player getTrackedSpeedRunner()
    {
        return ClientSpeedRunnerDistance.player;
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
