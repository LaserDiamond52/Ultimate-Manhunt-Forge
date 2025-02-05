package net.laserdiamond.reversemanhunt;

import net.laserdiamond.laserutils.util.raycast.AbstractRayCast;
import net.laserdiamond.reversemanhunt.capability.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.capability.PlayerSpeedRunner;
import net.laserdiamond.reversemanhunt.event.HuntersReleasedEvent;
import net.laserdiamond.reversemanhunt.event.ReverseManhuntGameStateEvent;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.game.GameTimeS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.ClosestSpeedRunnerS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.CloseDistanceFromHunterS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.HunterDetectionS2CPacket;
import net.laserdiamond.reversemanhunt.sound.RMSoundEvents;
import net.laserdiamond.reversemanhunt.util.RMMath;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID)
public class RMGameState {

    /**
     * The current {@linkplain State state} of the Reverse Manhunt game on the SERVER
     */
    private static State currentGameState = State.NOT_STARTED;

    /**
     * The time in ticks that speed runners have a head start before hunters are released
     */
    public static final int HUNTER_GRACE_PERIOD_TICKS = 1800; // 90 seconds

    /**
     * The time in ticks that speed runners cannot be harmed by a hunter after being killed by a hunter
     */
    public static final int SPEED_RUNNER_GRACE_PERIOD_TICKS = 600; // 30 seconds

    /**
     * Amount of lives speed runners have
     */
    public static final int SPEED_RUNNER_LIVES = 3; // Speed runners have 3 lives

    /**
     * Friendly fire
     */
    public static final boolean FRIENDLY_FIRE = true; // Determines if speed runners can attack other speed runners, and if hunters can attack other hunters

    /**
     * Determines if speed runners lose a life if they die from a cause unrelated to a hunter
     */
    public static final boolean HARDCORE = false; // Determines if a speed runner loses a life if they die at all

    /**
     * Hunter's tracking distance to find speed runners
     */
    private static final int HUNTER_TRACKING_RANGE = 5000;

    /**
     * Detection range for hunters for speed runners
     */
    private static final int HUNTER_DETECTION_RANGE = 50;

    /**
     * A {@link Set} of player UUIDs for the people currently in an iteration of the game
     */
    private static final Set<UUID> LOGGED_PLAYER_UUIDS = new HashSet<>();

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

    /**
     * @return True if the {@linkplain #currentGameTime} is still less than the {@linkplain #HUNTER_GRACE_PERIOD_TICKS hunter grace period time stamp}
     */
    public static boolean areHuntersOnGracePeriod()
    {
        return currentGameTime < HUNTER_GRACE_PERIOD_TICKS;
    }

    /**
     * @return A {@link Set} of player UUIDs for the current iteration of the game
     */
    public static Set<UUID> getLoggedPlayerUUIDs()
    {
        return new HashSet<>(LOGGED_PLAYER_UUIDS);
    }

    /**
     * Wipes all the player UUIDs from the {@linkplain #LOGGED_PLAYER_UUIDS Logged Player UUIDs}
     */
    public static void wipeLoggedPlayerUUIDs()
    {
        LOGGED_PLAYER_UUIDS.clear();
    }

    /**
     * Adds a {@linkplain Player player's} UUID to the current {@linkplain #LOGGED_PLAYER_UUIDS set of logged players}
     * @param player The {@linkplain Player player} to add
     */
    public static void logPlayerUUID(Player player)
    {
        LOGGED_PLAYER_UUIDS.add(player.getUUID());
    }

    /**
     * Checks if the {@linkplain Player player} is logged for the current iteration of the game
     * @param player The {@linkplain Player player} to check
     * @return True if the {@linkplain Player player} is currently logged, false otherwise
     */
    public static boolean containsLoggedPlayerUUID(Player player)
    {
        return LOGGED_PLAYER_UUIDS.contains(player.getUUID());
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
            RMPackets.sendToAllClients(new GameTimeS2CPacket(currentGameTime)); // Send current time to all client

        }
    }

    @SubscribeEvent
    public static void onPlayerServerTick(TickEvent.PlayerTickEvent.Post event)
    {
        Player player = event.player;

        if (event.side == LogicalSide.CLIENT)
        {
            return;
        }

        Level level = player.level();
        if (level.isClientSide)
        {
            return;
        }

        if (currentGameTime == HUNTER_GRACE_PERIOD_TICKS) // Has the grace period just ended?
        {
            MinecraftForge.EVENT_BUS.post(new HuntersReleasedEvent(PlayerHunter.getHunters(), PlayerSpeedRunner.getRemainingSpeedRunners())); // Post release event
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
                    if (currentGameTime < HUNTER_GRACE_PERIOD_TICKS)
                    {
                        player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterSpawnAttributes());
                    }
                    HashMap<UUID, Float> playerDistances = new HashMap<>();
                    for (Player nearbyPlayer : level.getEntitiesOfClass(Player.class, AbstractRayCast.createBBLivingEntity(player, HUNTER_TRACKING_RANGE), RMGameState::isSpeedRunner))
                    {
                        Level nearLevel = nearbyPlayer.level();
                        if (!level.dimension().equals(nearLevel.dimension()))
                        {
                            continue;
                        }
                        float distance = player.distanceTo(nearbyPlayer);
                        playerDistances.put(nearbyPlayer.getUUID(), distance);
                        RMPackets.sendToPlayer(new CloseDistanceFromHunterS2CPacket(distance), nearbyPlayer); // Tell nearby player how far they are from the hunter

                        if (currentGameTime > HUNTER_GRACE_PERIOD_TICKS) // Is hunter out of grace period?
                        {
                            if (distance < HUNTER_DETECTION_RANGE) // Is the nearby player close enough to the hunter to be notified?
                            {
                                if (nearbyPlayer instanceof ServerPlayer nearServerPlayer)
                                {
                                    if (nearbyPlayer.isAlive()) // Is the player alive?
                                    {
                                        if (nearbyPlayer.tickCount % 6 == 0) // ~180 bpm
                                        {
                                            nearServerPlayer.connection.send(new ClientboundSoundPacket(RMSoundEvents.HEART_BEAT.getHolder().get(), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 100, 1.0F, level.getRandom().nextLong()));
                                        }
                                        RMSoundEvents.playDetectionSound(nearbyPlayer); // Play detection sound
                                    } else // Player is not alive
                                    {
                                        if (nearbyPlayer.tickCount % 120 == 0)
                                        {
                                            RMSoundEvents.playFlatlineSound(player);
                                        }
                                    }
                                }
                            } else // Not close enough to notify hunter
                            {
                                RMSoundEvents.stopDetectionSound(nearbyPlayer);
                            }
                        }
                    }

                    if (playerDistances.isEmpty())
                    {
                        RMPackets.sendToPlayer(new ClosestSpeedRunnerS2CPacket(false, player.getUUID(), 0F), player);
                        return;
                    }

                    float smallestDistance = RMMath.getLeast(playerDistances.values().stream().toList());
                    for (Map.Entry<UUID, Float> entry : playerDistances.entrySet())
                    {
                        if (entry.getValue() == smallestDistance)
                        {
                            RMPackets.sendToPlayer(new ClosestSpeedRunnerS2CPacket(true, entry.getKey(), entry.getValue()), player);
                            return;
                        }
                    }
                }
            } else // Player is not a hunter
            {
//                if (State.isGameRunning())
//                {
//                    if (currentGameTime > HUNTER_GRACE_PERIOD_TICKS) // Is hunter grace period over?
//                    {
//                        List<Player> hunters = level.getEntitiesOfClass(Player.class, AbstractRayCast.createBBLivingEntity(player, HUNTER_DETECTION_RANGE), RMGameState::isHunter);
//
//                        if (player instanceof  ServerPlayer serverPlayer)
//                        {
//                            if (!hunters.isEmpty()) // Was a hunter near?
//                            {
//                                // Hunter nearby
//                                if (player.tickCount % 6 == 0) // ~180 bpm
//                                {
//                                    serverPlayer.connection.send(new ClientboundSoundPacket(RMSoundEvents.HEART_BEAT.getHolder().get(), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 100, 1.0F, level.getRandom().nextLong()));
//                                }
//                                RMSoundEvents.playDetectionSound(player);
//
//                            } else // No hunter near
//                            {
//                                RMSoundEvents.stopDetectionSound(player);
//
//                            }
//                        }
//
//                    }
//                }
            }
        });
    }

    private static boolean isSpeedRunner(Player player)
    {
        for (Player target : PlayerSpeedRunner.getRemainingSpeedRunners())
        {
            if (target.getUUID() == player.getUUID())
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isHunter(Player player)
    {
        for (Player target : PlayerHunter.getHunters())
        {
            if (target.getUUID() == player.getUUID())
            {
                return true;
            }
        }
        return false;
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
         * A Reverse Manhunt game is currently in progress. This state is reached if the game was previously in a {@linkplain #PAUSED paused} state after resuming the game using the {@linkplain net.laserdiamond.reversemanhunt.commands.ReverseManhuntGameCommands Reverse Manhunt Game Command}
         */
        IN_PROGRESS,

        /**
         * A Reverse Manhunt game is on pause.
         * This state is reached if the game is put on pause by the use of the {@linkplain net.laserdiamond.reversemanhunt.commands.ReverseManhuntGameCommands Reverse Manhunt Game Command}.
         * <p>While the Reverse Manhunt game is in this state, Speed Runners cannot lose lives, the Ender Dragon cannot be damaged, and Hunters cannot track speed runners</p>
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

        public static State fromOrdinal(int value)
        {
            for (State state : values())
            {
                if (state.ordinal() == value)
                {
                    return state;
                }
            }
            return null;
        }
    }

}
