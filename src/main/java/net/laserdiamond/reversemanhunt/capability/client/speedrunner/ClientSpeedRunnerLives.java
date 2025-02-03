package net.laserdiamond.reversemanhunt.capability.client.speedrunner;

import net.laserdiamond.reversemanhunt.RMGameState;

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
