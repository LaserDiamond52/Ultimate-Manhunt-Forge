package net.laserdiamond.ultimatemanhunt.client.hunter;

import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.UUID;

public class ClientTrackedSpeedRunner {

    private static boolean speedRunnersPresent;
    private static String playerName;
    private static UUID playerUUID;
    private static Vector3f position;
    private static Vector3f oldPosition;
    private static float eyeHeight;
    private static long lastUpdateTick;
    private static long interval = 5;

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

    public static void setPosition(Vec3 position)
    {
        ClientTrackedSpeedRunner.position = position.toVector3f();
    }

    public static Vec3 getPosition()
    {
        Vector3f pos = ClientTrackedSpeedRunner.position;
        if (pos == null)
        {
            return new Vec3(0, 0, 0);
        }
        return new Vec3(ClientTrackedSpeedRunner.position);
    }

    public static void setOldPosition(Vec3 position)
    {
        ClientTrackedSpeedRunner.oldPosition = position.toVector3f();
    }

    public static Vec3 getOldPosition()
    {
        if (ClientTrackedSpeedRunner.oldPosition != null)
        {
            return new Vec3(ClientTrackedSpeedRunner.oldPosition);
        }
        return getPosition();
    }

    public static void setEyeHeight(float eyeHeight)
    {
        ClientTrackedSpeedRunner.eyeHeight = eyeHeight;
    }

    public static float getEyeHeight()
    {
        return ClientTrackedSpeedRunner.eyeHeight;
    }

    public static void setLastUpdateTick(long tick)
    {
        ClientTrackedSpeedRunner.lastUpdateTick = tick;
    }

    public static long getLastUpdateTick()
    {
        return ClientTrackedSpeedRunner.lastUpdateTick;
    }

    public static void setUpdateInterval(long interval)
    {
        ClientTrackedSpeedRunner.interval = interval;
    }

    public static long getUpdateInterval()
    {
        return ClientTrackedSpeedRunner.interval;
    }

    public static Vec3 getLerpedSpeedRunnerPosition(float partialTick)
    {
        Vec3 oldPos = getOldPosition();
        Vec3 currentPos = getPosition();
        Minecraft mc = Minecraft.getInstance();
        Level mcLevel = mc.level;
        if (mcLevel == null)
        {
            return currentPos;
        }
        long gameTime = mcLevel.getGameTime();
        float alpha = Mth.clamp((gameTime + partialTick - getLastUpdateTick()) / getUpdateInterval(), 0f, 1f);
        return oldPos.lerp(currentPos, alpha);
    }
}
