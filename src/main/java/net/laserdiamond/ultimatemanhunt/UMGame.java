package net.laserdiamond.ultimatemanhunt;

import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.laserdiamond.ultimatemanhunt.api.event.HuntersReleasedEvent;
import net.laserdiamond.ultimatemanhunt.api.event.UltimateManhuntGameStateEvent;
import net.laserdiamond.ultimatemanhunt.item.UMItems;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.game.GameStateS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.GameTimeS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.HardcoreUpdateS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.hunter.HunterGracePeriodDurationS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.hunter.TrackingSpeedRunnerS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerDistanceFromHunterS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerGracePeriodDurationS2CPacket;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID)
public class UMGame {

    /**
     * Detection range for hunters for speed runners
     */
    public static final int HUNTER_DETECTION_RANGE = 50;

    /**
     * The current {@linkplain State state} of the Ultimate Manhunt game on the SERVER
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
     * The x spawn coordinate of the Ultimate Manhunt
     */
    private static int xSpawnCoordinate = 0;

    /**
     * The z spawn coordinate of hte Ultimate Manhunt
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
     * Determines if the {@linkplain UMItems#WIND_TORCH Wind Torch} item is enabled
     */
    private static boolean windTorchEnabled = true; // Determines if the Wind Torch is enabled

    /**
     * The role the {@linkplain Player player} will be assigned when first joining the game
     */
    private static PlayerRole newPlayerRole = PlayerRole.SPECTATOR;

    /**
     * The role the {@linkplain Player player} will be assigned after losing all their lives as a speed runner
     */
    private static PlayerRole deadSpeedRunnerRole = PlayerRole.HUNTER;

    /**
     * A {@link Set} of player UUIDs for the people currently in an iteration of the game
     */
    private static final Set<UUID> LOGGED_PLAYER_UUIDS = new HashSet<>();

    /**
     * @return The current {@linkplain State game state} of the Ultimate Manhunt game
     */
    public static State getCurrentGameState()
    {
        return currentGameState;
    }

    private static long currentGameTime = 0;

    /**
     * Resets the current game time for the Ultimate Manhunt
     */
    public static void resetGameTime()
    {
        currentGameTime = 0;
        UMPackets.sendToAllClients(new GameTimeS2CPacket(0));
    }

    /**
     * @return The current game time of the Ultimate Manhunt
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
        UMGame.hunterGracePeriodTicks = durationTicks;
        UMPackets.sendToAllClients(new HunterGracePeriodDurationS2CPacket(durationTicks));
    }

    /**
     * Gets the duration of the hunter grace period
     * @return The duration of the hunter grace period
     */
    public static int getHunterGracePeriod()
    {
        return UMGame.hunterGracePeriodTicks;
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
        UMGame.speedRunnerGracePeriodTicks = durationTicks;
        UMPackets.sendToAllClients(new SpeedRunnerGracePeriodDurationS2CPacket(durationTicks));
    }

    /**
     * Gets the duration of the speed runner grace period
     * @return The duration of the speed runner grace period
     */
    public static int getSpeedRunnerGracePeriod()
    {
        return UMGame.speedRunnerGracePeriodTicks;
    }

    /**
     * Sets if friendly fire is enabled or disabled for the game
     * @param friendlyFire True if friendly fire is enabled, false otherwise
     */
    public static void setFriendlyFire(boolean friendlyFire)
    {
        UMGame.friendlyFire = friendlyFire;
    }

    /**
     * Gets if friendly fire is enabled
     * @return True if friendly fire is enabled. False otherwise
     */
    public static boolean isFriendlyFire()
    {
        return UMGame.friendlyFire;
    }

    /**
     * Sets if hardcore more is enabled for the game.
     * <p>In hardcore mode, speed runners will lose lives for all deaths, not just deaths from a hunter</p>
     * @param hardcore True if hardcore mode should be enabled, false otherwise
     */
    public static void setHardcore(boolean hardcore)
    {
        UMGame.hardcore = hardcore;
        UMPackets.sendToAllClients(new HardcoreUpdateS2CPacket(hardcore));
    }

    /**
     * Gets if hardcore mode is enabled
     * @return True if hardcore mode is enabled, false otherwise
     */
    public static boolean isHardcore()
    {
        return UMGame.hardcore;
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
     * Sets if the {@linkplain UMItems#WIND_TORCH Wind Torch} item is to be granted to speed runners
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

    public static void setNewPlayerRole(@NotNull PlayerRole playerRole)
    {
        newPlayerRole = playerRole;
    }

    public static PlayerRole getNewPlayerRole()
    {
        return newPlayerRole;
    }

    public static boolean setDeadSpeedRunnerRole(@NotNull PlayerRole playerRole)
    {
        if (playerRole == PlayerRole.SPEED_RUNNER)
        {
            return false; // Cannot set dead speed runner players to be speed runners again
        }
        deadSpeedRunnerRole = playerRole;
        return true;
    }

    public static PlayerRole getDeadSpeedRunnerRole()
    {
        return deadSpeedRunnerRole;
    }

    public static void sendMessageToAllPlayers(MinecraftServer minecraftServer, Component component)
    {
        minecraftServer.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(component));
    }

    @SubscribeEvent
    public static void onServerTickPre(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            return;
        }
        if (State.isGameRunning())
        {
            currentGameTime++; // Increment the current game time for as long as the game is running
            UMPackets.sendToAllClients(new GameTimeS2CPacket(currentGameTime)); // Send current time to all client
        }
    }

    @SubscribeEvent
    public static void onPlayerServerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            return;
        }
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
            MinecraftForge.EVENT_BUS.post(new HuntersReleasedEvent()); // Post release event
        }

        player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
        {
            if (umPlayer.isSpectator())
            {
                if (player instanceof ServerPlayer serverPlayer)
                {
                    serverPlayer.setGameMode(GameType.SPECTATOR);
                }
            } else if (umPlayer.isHunter())
            {
                if (State.isGameRunning())
                {
                    if (umPlayer.isBuffedHunter())
                    {
                        player.getFoodData().eat(200, 1.0F);
                        if (player.tickCount % 200 == 0)
                        {
                            player.setHealth(player.getHealth() + 2);

                        }
                    }
                    if (currentGameTime < hunterGracePeriodTicks)
                    {
                        player.teleportTo(xSpawnCoordinate, 1000, zSpawnCoordinate); // Hunters should be teleported to an unreachable place
                        return;
                    }
                    // Tracking players
                    List<Player> speedRunners = UMPlayer.getAvailableSpeedRunners(player);
                    if (speedRunners.isEmpty())
                    {
                        TrackingSpeedRunnerS2CPacket.sendNonTracking(player);
                        return;
                    }
                    for (Player speedRunnerPlayer : speedRunners)
                    {
                        if (UMPlayer.isSpeedRunnerOnGracePeriodServer(player))
                        {
                            continue; // Speed runner is on grace period. Do not continue
                        }
                        if (!player.isAlive()) // Is the hunter dead?
                        {
                            SpeedRunnerDistanceFromHunterS2CPacket.sendNotNearHunterPlayer(speedRunnerPlayer); // Hunter is dead
                            continue; // Skip to next iteration. Shouldn't notify player if hunter is dead
                        }
                        float distance = player.distanceTo(speedRunnerPlayer);
                        UMPackets.sendToPlayer(new SpeedRunnerDistanceFromHunterS2CPacket(distance), speedRunnerPlayer);

                        if (distance < HUNTER_DETECTION_RANGE) // Is the nearby player close enough to the hunter to be notified?
                        {
                            if (speedRunnerPlayer instanceof ServerPlayer nearServerPlayer)
                            {
                                if (speedRunnerPlayer.isAlive()) // Is the player alive?
                                {
                                    int rate = (int) ((distance / 12.5) + 6); // Rate ranges from 6 (closest) to 10 (furthest)
                                    if (speedRunnerPlayer.tickCount % rate == 0) // ~180 bpm
                                    {
                                        nearServerPlayer.connection.send(new ClientboundSoundPacket(UMSoundEvents.HEART_BEAT.getHolder().get(), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 100, 1.0F, level.getRandom().nextLong()));
                                    }
                                    UMSoundEvents.playDetectionSound(speedRunnerPlayer); // Play detection sound
                                }
                            }

                        } else // Not close enough to notify hunter
                        {
                            UMSoundEvents.stopDetectionSound(speedRunnerPlayer);
                        }

                    }

                    UUID trackedPlayerUUID = umPlayer.getTrackingPlayerUUID(); // UUID of player to track
                    if (trackedPlayerUUID == player.getUUID())
                    {
                        TrackingSpeedRunnerS2CPacket.sendNonTracking(player); // No player being tracked.
                        return;
                    }
                    MinecraftServer mcServer = player.getServer();
                    if (mcServer == null) // Is server null?
                    {
                        TrackingSpeedRunnerS2CPacket.sendNonTracking(player); // No player being tracked.
                        return;
                    }
                    Player trackedPlayer = mcServer.getPlayerList().getPlayer(trackedPlayerUUID); // Player to track
                    if (trackedPlayer == null) // Is there a player being tracked?
                    {
                        TrackingSpeedRunnerS2CPacket.sendNonTracking(player); // No player being tracked.
                        return;
                    }
                    if (!trackedPlayer.level().isClientSide) // On server for tracked player?
                    {
                        if (!trackedPlayer.level().dimension().equals(player.level().dimension())) // Are players in different dimensions?
                        {
                            TrackingSpeedRunnerS2CPacket.sendNonTracking(player); // Tracked Player and Hunter are not in the same dimension
                            return;
                        }
                        if (UMPlayer.isSpeedRunnerOnGracePeriodServer(trackedPlayer)) // Is the speed runner on grace period?
                        {
                            TrackingSpeedRunnerS2CPacket.sendNonTracking(player); // Tracked Player is on grace period
                            return;
                        }
                        if (!trackedPlayer.isAlive()) // Is the tracked player alive?
                        {
                            TrackingSpeedRunnerS2CPacket.sendNonTracking(player); // Tracked Player is dead
                            return;
                        }
                        LazyOptional<UMPlayer> trackedPlayerCap = trackedPlayer.getCapability(UMPlayerCapability.UM_PLAYER); // Get hunter capability of tracked player
                        if (trackedPlayerCap.isPresent()) // Is the capability present?
                        {
                            UMPlayer trackedPlayerHunter = trackedPlayerCap.orElse(new UMPlayer(trackedPlayerUUID));
                            if (!trackedPlayerHunter.isSpeedRunner()) // Is the tracked player NOT a speed runner (Player could change roles while being tracked)
                            {
                                TrackingSpeedRunnerS2CPacket.sendNonTracking(player); // Player is a hunter. Do not track
                                return;
                            }
                        }
                    }
                    float distance = player.distanceTo(trackedPlayer);
                    UMPackets.sendToPlayer(new TrackingSpeedRunnerS2CPacket(true, trackedPlayer, distance), player); // Hunter is now tracking this player

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
     * Sets the {@linkplain State game state} of the Ultimate Manhunt game
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
        UMPackets.sendToAllClients(new GameStateS2CPacket(currentGameState)); // Send to all clients
        return true;
    }

    /**
     * All possible game states for the Ultimate Manhunt
     */
    public enum State
    {
        /**
         * A Manhunt game has started. This state is reached when the Ultimate Manhunt game has been started using the {@linkplain net.laserdiamond.ultimatemanhunt.commands.sub.SetGameStateSC Ultimate Manhunt Game Command}
         */
        STARTED,

        /**
         * A Manhunt game is currently in progress. This state is reached if the game was previously in a {@linkplain #PAUSED paused} state after resuming the game using the {@linkplain net.laserdiamond.ultimatemanhunt.commands.sub.SetGameStateSC Ultimate Manhunt Game Command}
         */
        IN_PROGRESS,

        /**
         * A Manhunt game is on pause.
         * This state is reached if the game is put on pause by the use of the {@linkplain net.laserdiamond.ultimatemanhunt.commands.sub.SetGameStateSC Manhunt Game Command}.
         * <p>While the Manhunt game is in this state, Speed Runners cannot lose lives, the Ender Dragon cannot be damaged, and Hunters cannot track speed runners</p>
         */
        PAUSED,

        /**
         * A Manhunt game is not currently in progress yet, or has not been started.
         * This state is reached either through the use of the {@linkplain net.laserdiamond.ultimatemanhunt.commands.sub.SetGameStateSC Manhunt Game Command},
         * or if the {@linkplain UltimateManhuntGameStateEvent.End End Game Event} is fired
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
         * @return True if the Manhunt Game has been started
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

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public String getAsName()
        {
            String ret = this.toString().substring(0,1).toUpperCase() + this.toString().substring(1);
            ret = ret.replace("_", " ");
            boolean foundSpace = false;
            for (int i = 0; i < ret.length(); i++)
            {
                String s = ret.substring(i, Math.min(i + 1, ret.length()));
                if (s.equals(" "))
                {
                    foundSpace = true;
                    continue;
                }
                if (foundSpace)
                {
                    ret = ret.replace(" " + s, " " + s.toUpperCase());
                    foundSpace = false;
                }
            }
            return ret;
        }
    }

    public enum PlayerRole
    {
        SPECTATOR,
        SPEED_RUNNER,
        HUNTER;

        public static PlayerRole fromString(String value)
        {
            for (PlayerRole playerRole : PlayerRole.values())
            {
                if (value.equals(playerRole.toString()))
                {
                    return playerRole;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public String getAsName()
        {
            String ret = this.toString().substring(0,1).toUpperCase() + this.toString().substring(1);
            ret = ret.replace("_", " ");
            boolean foundSpace = false;
            for (int i = 0; i < ret.length(); i++)
            {
                String s = ret.substring(i, Math.min(i + 1, ret.length()));
                if (s.equals(" "))
                {
                    foundSpace = true;
                    continue;
                }
                if (foundSpace)
                {
                    ret = ret.replace(" " + s, " " + s.toUpperCase());
                    foundSpace = false;
                }
            }
            return ret;
        }
    }

}
