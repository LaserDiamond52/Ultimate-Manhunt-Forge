package net.laserdiamond.reversemanhunt;

import net.laserdiamond.reversemanhunt.capability.game.PlayerGameTimeCapability;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunner;
import net.laserdiamond.reversemanhunt.api.event.HuntersReleasedEvent;
import net.laserdiamond.reversemanhunt.api.event.ReverseManhuntGameStateEvent;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.game.GameTimeCapabilitySyncS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.game.GameTimeS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.TrackingSpeedRunnerS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.CloseDistanceFromHunterS2CPacket;
import net.laserdiamond.reversemanhunt.sound.RMSoundEvents;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID)
public class RMGame {

    /**
     * Detection range for hunters for speed runners
     */
    public static final int HUNTER_DETECTION_RANGE = 50;

    /**
     * The current {@linkplain State state} of the Reverse Manhunt game on the SERVER
     */
    private static State currentGameState = State.NOT_STARTED;

    /**
     * The time in ticks that speed runners have a head start before hunters are released
     */
    private static int hunterGracePeriodTicks = 1800; // 90 seconds

    /**
     * The time in ticks that speed runners cannot be harmed by a hunter after being killed by a hunter
     */
    private static int speedRunnerGracePeriodTicks = 600; // 30 seconds

    /**
     * The x spawn coordinate of the Reverse Manhunt
     */
    private static int xSpawnCoordinate = 0;

    /**
     * The z spawn coordinate of hte Reverse Manhunt
     */
    private static int zSpawnCoordinate = 0;

    /**
     * Friendly fire
     */
    private static boolean friendlyFire = true; // Determines if speed runners can attack other speed runners, and if hunters can attack other hunters

    /**
     * Determines if speed runners lose a life if they die from a cause unrelated to a hunter
     */
    private static boolean hardcore = false; // Determines if a speed runner loses a life if they die at all

    /**
     * Determines if the {@linkplain net.laserdiamond.reversemanhunt.item.RMItems#WIND_TORCH Wind Torch} item is enabled
     */
    private static boolean windTorchEnabled = true; // Determines if the Wind Torch is enabled

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
     * @return True if the {@linkplain #currentGameTime} is still less than the {@linkplain #hunterGracePeriodTicks hunter grace period time stamp}
     */
    public static boolean areHuntersOnGracePeriod()
    {
        return currentGameTime < hunterGracePeriodTicks;
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

    /**
     * Checks if the {@linkplain Player player} is a speed runner
     * @param player The {@linkplain Player player} to check
     * @return True if the {@linkplain Player player} is on grace period
     */
    public static boolean isSpeedRunnerOnGracePeriod(Player player)
    {
        AtomicBoolean ret = new AtomicBoolean(false);
        player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
        {
            if (playerHunter.isHunter()) // Is the player a hunter?
            {
                return; // Player is a hunter. Do not continue here
            }
            player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
            {
                if (!playerSpeedRunner.getWasLastKilledByHunter())
                {
                    return; // Player wasn't last killed by a hunter
                }
                ret.set(currentGameTime < playerSpeedRunner.getGracePeriodTimeStamp()); // Player is on speed runner grace period if the current game time is less than the time stamp of their grace period
            });
        });
        return ret.get();
    }

    /**
     * Sets the grace period for hunters at the start of the game
     * @param durationTicks The duration in ticks of the grace period.
     *                      If the value is 0 or less, the grace period duration will not change.
     */
    public static void setHunterGracePeriod(int durationTicks)
    {
        if (durationTicks <= 0)
        {
            return;
        }
        RMGame.hunterGracePeriodTicks = durationTicks;
    }

    /**
     * Gets the duration of the hunter grace period
     * @return The duration of the hunter grace period
     */
    public static int getHunterGracePeriod()
    {
        return RMGame.hunterGracePeriodTicks;
    }

    /**
     * Sets the grace period for speed runners after they die from a hunter
     * @param durationTicks The duration in ticks of the grace period.
     *                      If the value is 0 or less, the grace period duration will not change.
     */
    public static void setSpeedRunnerGracePeriod(int durationTicks)
    {
        if (durationTicks <= 0)
        {
            return;
        }
        RMGame.speedRunnerGracePeriodTicks = durationTicks;
    }

    /**
     * Gets the duration of the speed runner grace period
     * @return The duration of the speed runner grace period
     */
    public static int getSpeedRunnerGracePeriod()
    {
        return RMGame.speedRunnerGracePeriodTicks;
    }

    /**
     * Sets if friendly fire is enabled or disabled for the game
     * @param friendlyFire True if friendly fire is enabled, false otherwise
     */
    public static void setFriendlyFire(boolean friendlyFire)
    {
        RMGame.friendlyFire = friendlyFire;
    }

    /**
     * Gets if friendly fire is enabled
     * @return True if friendly fire is enabled. False otherwise
     */
    public static boolean isFriendlyFire()
    {
        return RMGame.friendlyFire;
    }

    /**
     * Sets if hardcore more is enabled for the game.
     * <p>In hardcore mode, speed runners will lose lives for all deaths, not just deaths from a hunter</p>
     * @param hardcore True if hardcore mode should be enabled, false otherwise
     */
    public static void setHardcore(boolean hardcore)
    {
        RMGame.hardcore = hardcore;
    }

    /**
     * Gets if hardcore mode is enabled
     * @return True if hardcore mode is enabled, false otherwise
     */
    public static boolean isHardcore()
    {
        return RMGame.hardcore;
    }

    /**
     * Sets the X and Z spawn coordinate for speed runners and hunters when the game starts
     * @param x The x coordinate to spawn at
     * @param z The z coordinate to spawn at
     */
    public static void setXAndZSpawnCoordinate(int x, int z)
    {
        xSpawnCoordinate = x;
        zSpawnCoordinate = z;
    }

    public static int getXSpawnCoordinate()
    {
        return xSpawnCoordinate;
    }

    public static int getZSpawnCoordinate()
    {
        return zSpawnCoordinate;
    }

    /**
     * Sets if the {@linkplain net.laserdiamond.reversemanhunt.item.RMItems#WIND_TORCH Wind Torch} item is to be granted to speed runners
     * @param enabled True if the item should be granted, false otherwise
     */
    public static void setWindTorchEnabled(boolean enabled)
    {
        windTorchEnabled = enabled;
    }

    public static boolean isWindTorchEnabled()
    {
        return windTorchEnabled;
    }

    @SubscribeEvent
    public static void onServerTickPre(TickEvent.ServerTickEvent.Pre event)
    {
        if (State.isGameRunning())
        {
            currentGameTime++; // Increment the current game time for as long as the game is running
            RMPackets.sendToAllClients(new GameTimeS2CPacket(currentGameTime)); // Send current time to all client
            for (Player player : event.getServer().getPlayerList().getPlayers())
            {
                player.getCapability(PlayerGameTimeCapability.PLAYER_GAME_TIME).ifPresent(playerGameTime ->
                {
                    playerGameTime.setGameTime(currentGameTime);
                    RMPackets.sendToAllTrackingEntityAndSelf(new GameTimeCapabilitySyncS2CPacket(player.getId(), playerGameTime.toNBT()), player);
                });
            }
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

        if (currentGameTime == hunterGracePeriodTicks) // Has the grace period just ended?
        {
            MinecraftForge.EVENT_BUS.post(new HuntersReleasedEvent(PlayerHunter.getHunters(), PlayerSpeedRunner.getRemainingSpeedRunners())); // Post release event
        }

        player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
        {
            if (playerHunter.isHunter()) // Is the player a hunter?
            {
                if (State.isGameRunning()) // Is a game in progress?
                {
                    player.getFoodData().eat(200, 1.0F);

                    if (playerHunter.isBuffed())
                    {
//                        player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterAttributes());
                        if (player.tickCount % 200 == 0)
                        {
                            player.setHealth(player.getHealth() + 2);

                        }
                    }
                    if (currentGameTime < hunterGracePeriodTicks)
                    {
                        player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterSpawnAttributes());
                        player.teleportTo(xSpawnCoordinate, 1000, zSpawnCoordinate); // Hunters should be teleported to an unreachable place
                        return; // End method here. Don't start tracking until hunters have been released
                    }
                    // Track chosen player
                    for (Player speedRunnerPlayer : PlayerHunter.getAvailableSpeedRunners(player)) // Let speed runners know how close they are to a hunter
                    {
                        if (PlayerSpeedRunner.isSpeedRunnerOnGracePeriod(speedRunnerPlayer))
                        {
                            continue; // Speed runner is on grace period. Do not continue
                        }
                        float distance = player.distanceTo(speedRunnerPlayer);
                        RMPackets.sendToPlayer(new CloseDistanceFromHunterS2CPacket(distance), speedRunnerPlayer);

                        if (currentGameTime > hunterGracePeriodTicks) // Is hunter out of grace period?
                        {
                            if (distance < HUNTER_DETECTION_RANGE) // Is the nearby player close enough to the hunter to be notified?
                            {
                                if (speedRunnerPlayer instanceof ServerPlayer nearServerPlayer)
                                {
                                    if (speedRunnerPlayer.isAlive()) // Is the player alive?
                                    {
                                        int rate = (int) ((distance / 12.5) + 6); // Rate ranges from 6 (closest) to 10 (furthest)
                                        if (speedRunnerPlayer.tickCount % rate == 0) // ~180 bpm
                                        {
                                            nearServerPlayer.connection.send(new ClientboundSoundPacket(RMSoundEvents.HEART_BEAT.getHolder().get(), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 100, 1.0F, level.getRandom().nextLong()));
                                        }
                                        RMSoundEvents.playDetectionSound(speedRunnerPlayer); // Play detection sound
                                    }
                                }

                            } else // Not close enough to notify hunter
                            {
                                RMSoundEvents.stopDetectionSound(speedRunnerPlayer);
                            }
                        }
                    }
                    UUID trackedPlayerUUID = playerHunter.getTrackingPlayerUUID(); // UUID of player to track
                    if (trackedPlayerUUID == player.getUUID())
                    {
                        RMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(false, player, 0F), player); // No player being tracked.
                        return;
                    }
                    MinecraftServer mcServer = player.getServer();
                    if (mcServer == null) // Is server null?
                    {
                        RMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(false, player, 0F), player); // No player being tracked.
                        return;
                    }
                    Player trackedPlayer = mcServer.getPlayerList().getPlayer(trackedPlayerUUID); // Player to track
                    if (trackedPlayer == null) // Is there a player being tracked?
                    {
                        RMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(false, player, 0F), player); // No player being tracked.
                        return;
                    }
                    if (!trackedPlayer.level().isClientSide) // On server for tracked player?
                    {
                        if (!trackedPlayer.level().dimension().equals(player.level().dimension())) // Are players in different dimensions?
                        {
                            RMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(false, player, 0F), player); // Tracked Player and Hunter are not in the same dimension
                            return;
                        }
                        if (PlayerSpeedRunner.isSpeedRunnerOnGracePeriod(trackedPlayer)) // Is the speed runner on grace period?
                        {
                            RMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(false, player, 0F), player); // Tracked Player is on grace period
                            return;
                        }
                        if (!trackedPlayer.isAlive()) // Is the tracked player alive?
                        {
                            RMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(false, player, 0F), player); // Tracked Player is dead
                            return;
                        }
                        LazyOptional<PlayerHunter> trackedPlayerHunterCap = trackedPlayer.getCapability(PlayerHunterCapability.PLAYER_HUNTER); // Get hunter capability of tracked player
                        if (trackedPlayerHunterCap.isPresent()) // Is the capability present?
                        {
                            PlayerHunter trackedPlayerHunter = trackedPlayerHunterCap.orElse(new PlayerHunter(trackedPlayerUUID));
                            if (trackedPlayerHunter.isHunter()) // Is the tracked player a hunter (Player can become a hunter while being tracked)
                            {
                                RMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(false, player, 0F), player); // Player is a hunter. Do not track
                                return;
                            }
                        }
                    }
                    float distance = player.distanceTo(trackedPlayer);
                    RMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(true, trackedPlayer, distance), player); // Hunter is now tracking this player

                    // Track Nearest Player
//                    HashMap<Integer, Float> playerDistances = new HashMap<>();
//                    HashMap<Integer, Vec3> playerPositions = new HashMap<>();
//                    for (Player speedRunnerPlayer : PlayerSpeedRunner.getRemainingSpeedRunners()) // Loop through all remaining speed runners
//                    {
//                        Level nearLevel = speedRunnerPlayer.level();
//                        if (!level.dimension().equals(nearLevel.dimension()))
//                        {
//                            continue; // Dimensions do not match. Skip this iteration
//                        }
//                        if (PlayerSpeedRunner.isSpeedRunnerOnGracePeriod(speedRunnerPlayer))
//                        {
//                            continue; // Do not track players that are on grace period
//                        }
//                        float distance = player.distanceTo(speedRunnerPlayer);
//                        playerDistances.put(speedRunnerPlayer.getId(), distance); // Save distance
//                        playerPositions.put(speedRunnerPlayer.getId(), speedRunnerPlayer.getEyePosition()); // Save position
//                        RMPackets.sendToPlayer(new CloseDistanceFromHunterS2CPacket(distance), speedRunnerPlayer); // Tell nearby player how far they are from a hunter
//
//                        if (currentGameTime > hunterGracePeriodTicks) // Is hunter out of grace period?
//                        {
//                            if (distance < HUNTER_DETECTION_RANGE) // Is the nearby player close enough to the hunter to be notified?
//                            {
//                                if (speedRunnerPlayer instanceof ServerPlayer nearServerPlayer)
//                                {
//                                    if (speedRunnerPlayer.isAlive()) // Is the player alive?
//                                    {
//                                        int rate = (int) ((distance / 12.5) + 6); // Rate ranges from 6 (closest) to 10 (furthest)
//                                        if (speedRunnerPlayer.tickCount % rate == 0) // ~180 bpm
//                                        {
//                                            nearServerPlayer.connection.send(new ClientboundSoundPacket(RMSoundEvents.HEART_BEAT.getHolder().get(), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 100, 1.0F, level.getRandom().nextLong()));
//                                        }
//                                        RMSoundEvents.playDetectionSound(speedRunnerPlayer); // Play detection sound
//                                    }
//                                }
//
//                            } else // Not close enough to notify hunter
//                            {
//                                RMSoundEvents.stopDetectionSound(speedRunnerPlayer);
//                            }
//                        }
//                    }
//
//                    if (playerDistances.isEmpty() || playerPositions.isEmpty())
//                    {
//                        RMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(false, player.getId(), 0F, player.getEyePosition()), player);
//                        return;
//                    }
//
//                    float smallestDistance = RMMath.getLeast(playerDistances.values().stream().toList());
//                    for (Map.Entry<Integer, Float> entry : playerDistances.entrySet())
//                    {
//                        if (entry.getValue() == smallestDistance)
//                        {
//                            RMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(true, entry.getKey(), entry.getValue(), playerPositions.get(entry.getKey())), player);
//                            return;
//                        }
//                    }
                }
            }
        });
    }

    public static boolean isNearHunter(Player playerSpeedRunner, Player playerHunter)
    {
        return playerSpeedRunner.distanceTo(playerHunter) < HUNTER_DETECTION_RANGE;
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
