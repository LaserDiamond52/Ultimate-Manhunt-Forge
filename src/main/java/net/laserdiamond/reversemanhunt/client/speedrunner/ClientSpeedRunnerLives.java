package net.laserdiamond.reversemanhunt.client.speedrunner;

import net.laserdiamond.reversemanhunt.RMGameState;
import net.laserdiamond.reversemanhunt.capability.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.client.hunter.ClientHunter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientSpeedRunnerLives {

    private static int lives;
    private static boolean wasLastKilledByHunter;

    public static void setLives(int lives)
    {
        ClientSpeedRunnerLives.lives = Math.max(0, Math.min(lives, RMGameState.SPEED_RUNNER_LIVES));
    }

    public static int getLives()
    {
        return ClientSpeedRunnerLives.lives;
    }

    public static void setWasLastKilledByHunter(boolean wasLastKilledByHunter)
    {
        ClientSpeedRunnerLives.wasLastKilledByHunter = wasLastKilledByHunter;
    }

    public static boolean getWasLastKilledByHunter()
    {
        return ClientSpeedRunnerLives.wasLastKilledByHunter;
    }
}
