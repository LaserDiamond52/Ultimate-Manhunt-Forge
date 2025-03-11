package net.laserdiamond.reversemanhunt.client.hunter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ClientTrackedSpeedRunner {

    private static boolean speedRunnersPresent;
    private static String playerName;
    private static UUID playerUUID;
    private static float distance;
    private static Vec3 position;

    public static void setSpeedRunnersPresent(boolean speedRunnersPresent)
    {
        ClientTrackedSpeedRunner.speedRunnersPresent = speedRunnersPresent;
    }

    public static boolean areSpeedRunnersPresent()
    {
        return ClientTrackedSpeedRunner.speedRunnersPresent;
    }

    public static void setTrackedPlayerName(String playerName)
    {
        ClientTrackedSpeedRunner.playerName = playerName;
    }

    public static String getTrackedPlayerName()
    {
        return ClientTrackedSpeedRunner.playerName;
    }

    public static void setTrackedPlayerUUID(UUID playerUUID)
    {
        ClientTrackedSpeedRunner.playerUUID = playerUUID;
    }

    public static UUID getTrackedPlayerUUID()
    {
        return ClientTrackedSpeedRunner.playerUUID;
    }

    public static void setDistance(float distance)
    {
        ClientTrackedSpeedRunner.distance = distance;
    }

    public static float getDistance()
    {
        return ClientTrackedSpeedRunner.distance;
    }

    public static void setPosition(Vec3 position)
    {
        ClientTrackedSpeedRunner.position = position;
    }

    public static Vec3 getPosition()
    {
        return ClientTrackedSpeedRunner.position;
    }
}
