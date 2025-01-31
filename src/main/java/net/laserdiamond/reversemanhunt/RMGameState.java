package net.laserdiamond.reversemanhunt;

import net.laserdiamond.reversemanhunt.capability.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.event.HuntersReleasedEvent;
import net.laserdiamond.reversemanhunt.event.ReverseManhuntGameStateEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID)
public class RMGameState {

    private static State currentGameState = State.NOT_STARTED;

    public static final int HUNTER_GRACE_PERIOD_TICKS = 6000; // 5 minutes

    public static final int SPEED_RUNNER_GRACE_PERIOD_TICKS = 2400; // 2 minutes

    public static final int SPEED_RUNNER_LIVES = 3; // Speed runners have 3 lives

    public static final boolean FRIENDLY_FIRE = true; // Determines if speed runners can attack other speed runners, and if hunters can attack other hunters

    /**
     * @return The current {@linkplain State game state} of the Reverse Manhunt game
     */
    public static State getCurrentGameState()
    {
        return currentGameState;
    }

    private static long currentGameTime = 0;

    /**
     * Resets the current game time for the Reverse Manhunt
     */
    public static void resetGameTime()
    {
        currentGameTime = 0;
    }

    /**
     * @return The current game time of the Reverse Manhunt
     */
    public static long getCurrentGameTime()
    {
        return currentGameTime;
    }

    public static boolean areHuntersOnGracePeriod()
    {
        return currentGameTime < HUNTER_GRACE_PERIOD_TICKS;
    }

    public static boolean isSpeedRunnerOnGracePeriod(Player player)
    {
        AtomicBoolean ret = new AtomicBoolean(false);
        player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
        {
            if (playerHunter.isHunter()) // Is the player a hunter?
            {
                return; // Player is a hunter. Do not continue here
            }
            ret.set(player.tickCount < SPEED_RUNNER_GRACE_PERIOD_TICKS);
        });
        return ret.get();
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent.Post event)
    {
        if (State.isGameRunning())
        {
            currentGameTime++; // Increment the current game time for as long as the game is running
//            ReverseManhunt.LOGGER.info("Current game time: " + currentGameTime);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event)
    {
        Player player = event.player;

        if (event.side == LogicalSide.CLIENT)
        {
            return;
        }

        player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
        {
            if (playerHunter.isHunter()) // Is the player a hunter?
            {
                player.getFoodData().eat(200, 1.0F);

                if (playerHunter.isBuffed())
                {
                    if (player.tickCount % 200 == 0)
                    {
                        player.setHealth(player.getHealth() + 2);
                    }
                }

                if (State.isGameRunning()) // Is a game in progress?
                {
//                    if (areHuntersOnGracePeriod()) // Are hunters on grace period
//                    {
//                        player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterSpawnAttributes()); // On grace period. Add attributes
//                    }
                    if (currentGameTime == HUNTER_GRACE_PERIOD_TICKS) // Has the grace period just ended?
                    {
                        MinecraftForge.EVENT_BUS.post(new HuntersReleasedEvent(PlayerHunter.getHunters())); // Post release event
                    }
                }

            }
        });
    }

    /**
     * Sets the {@linkplain State game state} of the Reverse Manhunt game
     * @param newGameState The new {@linkplain State state} to set the game into.
     * @return True if the {@linkplain State game state} was successfully changed.
     * Returns false if the current state is {@linkplain State#NOT_STARTED Not Started} and the new state to set is {@linkplain State#PAUSED Paused},
     * or if the new state to set the game into is the same as the current state
     */
    public static boolean setCurrentGameState(@NotNull State newGameState)
    {
        if (currentGameState == newGameState) // Game states are the same. Do not change
        {
            return false;
        }
        if (currentGameState == State.NOT_STARTED && newGameState == State.PAUSED) // Cannot pause a game that has not started
        {
            return false;
        }
        if (currentGameState == State.NOT_STARTED && newGameState == State.IN_PROGRESS) // Cannot resume a game that has not started
        {
            return false;
        }
        if ((currentGameState == State.PAUSED || currentGameState == State.IN_PROGRESS) && newGameState == State.STARTED) // Cannot start a new game if one is currently in progress
        {
            return false;
        }
        currentGameState = newGameState;
        return true;
    }

    /**
     * All possible game states for the Reverse Manhunt
     */
    public enum State
    {
        /**
         * A Reverse Manhunt game has started. This state is reached when the Reverse Manhunt game has been started using the {@linkplain net.laserdiamond.reversemanhunt.commands.ReverseManhuntGameCommands Reverse Manhunt Game Command}
         */
        STARTED,

        /**
         * A Reverse Manhunt game is currently in progress. This state is reached if the game was previously in a {@linkplain #PAUSED paused} state after using the {@linkplain net.laserdiamond.reversemanhunt.commands.ReverseManhuntGameCommands Reverse Manhunt Game Command}
         */
        IN_PROGRESS,

        /**
         * A Reverse Manhunt game is on pause.
         * This state is reached if the game is put on pause by the use of the {@linkplain net.laserdiamond.reversemanhunt.commands.ReverseManhuntGameCommands Reverse Manhunt Game Command}.
         * <p>While the Reverse Manhunt game is in this state, Speed Runners cannot lose lives, and the Ender Dragon cannot be damaged</p>
         */
        PAUSED,

        /**
         * A Reverse Manhunt game is not currently in progress yet, or has not been started.
         * This state is reached either through the use of the {@linkplain net.laserdiamond.reversemanhunt.commands.ReverseManhuntGameCommands Reverse Manhunt Game Command},
         * or if the {@linkplain ReverseManhuntGameStateEvent.End End Game Event} is fired
         */
        NOT_STARTED;

        /**
         * @return True if the {@linkplain #currentGameState current game state} is either {@linkplain #STARTED started} or {@linkplain #IN_PROGRESS in progress}
         */
        public static boolean isGameRunning()
        {
            return currentGameState == STARTED || currentGameState == IN_PROGRESS;
        }

        /**
         * @return True if the {@linkplain #currentGameState current game state} is either {@linkplain #PAUSED paused} or {@link #NOT_STARTED not started}
         */
        public static boolean isGameNotInProgress()
        {
            return currentGameState == PAUSED || currentGameState == NOT_STARTED;
        }

        /**
         * @return True if the Reverse Manhunt Game has been started
         */
        public static boolean hasGameBeenStarted()
        {
            return isGameRunning() || currentGameState == PAUSED;
        }
    }
}
