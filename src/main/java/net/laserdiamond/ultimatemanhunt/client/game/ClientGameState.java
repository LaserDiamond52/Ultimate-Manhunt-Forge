package net.laserdiamond.ultimatemanhunt.client.game;

import net.laserdiamond.ultimatemanhunt.UMGame;

/**
 * {@linkplain UMGame.State Game state} on the CLIENT
 */
public class ClientGameState {

    private static UMGame.State gameState;

    public static void setGameState(UMGame.State gameState)
    {
        ClientGameState.gameState = gameState;
    }

    public static UMGame.State getGameState()
    {
        return ClientGameState.gameState;
    }

    public static boolean isGameRunning()
    {
        return ClientGameState.gameState == UMGame.State.STARTED || ClientGameState.gameState == UMGame.State.IN_PROGRESS;
    }

    public static boolean isGameNotInProgress()
    {
        return ClientGameState.gameState == UMGame.State.PAUSED || ClientGameState.gameState == UMGame.State.NOT_STARTED;
    }

    public static boolean hasGameBeenStarted()
    {
        return isGameRunning() || ClientGameState.gameState == UMGame.State.PAUSED;
    }
}
