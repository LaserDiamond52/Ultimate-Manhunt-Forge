package net.laserdiamond.reversemanhunt.client.game;

import net.laserdiamond.reversemanhunt.RMGame;

/**
 * {@linkplain RMGame.State Game state} on the CLIENT
 */
public class ClientGameState {

    private static RMGame.State gameState;

    public static void setGameState(RMGame.State gameState)
    {
        ClientGameState.gameState = gameState;
    }

    public static RMGame.State getGameState()
    {
        return ClientGameState.gameState;
    }

    public static boolean isGameRunning()
    {
        return ClientGameState.gameState == RMGame.State.STARTED || ClientGameState.gameState == RMGame.State.IN_PROGRESS;
    }

    public static boolean isGameNotInProgress()
    {
        return ClientGameState.gameState == RMGame.State.PAUSED || ClientGameState.gameState == RMGame.State.NOT_STARTED;
    }

    public static boolean hasGameBeenStarted()
    {
        return isGameRunning() || ClientGameState.gameState == RMGame.State.PAUSED;
    }
}
