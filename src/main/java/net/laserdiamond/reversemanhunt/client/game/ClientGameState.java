package net.laserdiamond.reversemanhunt.client.game;

import net.laserdiamond.reversemanhunt.RMGameState;

/**
 * {@linkplain RMGameState.State Game state} on the CLIENT
 */
public class ClientGameState {

    private static RMGameState.State gameState;

    public static void setGameState(RMGameState.State gameState)
    {
        ClientGameState.gameState = gameState;
    }

    public static RMGameState.State getGameState()
    {
        return ClientGameState.gameState;
    }

    public static boolean isGameRunning()
    {
        return ClientGameState.gameState == RMGameState.State.STARTED || ClientGameState.gameState == RMGameState.State.IN_PROGRESS;
    }

    public static boolean isGameNotInProgress()
    {
        return ClientGameState.gameState == RMGameState.State.PAUSED || ClientGameState.gameState == RMGameState.State.NOT_STARTED;
    }

    public static boolean hasGameBeenStarted()
    {
        return isGameRunning() || ClientGameState.gameState == RMGameState.State.PAUSED;
    }
}
