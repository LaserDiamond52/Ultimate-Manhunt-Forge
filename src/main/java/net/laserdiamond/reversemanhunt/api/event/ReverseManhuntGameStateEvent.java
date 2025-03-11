package net.laserdiamond.reversemanhunt.api.event;

import net.laserdiamond.reversemanhunt.RMGame;
import net.laserdiamond.reversemanhunt.capability.game.PlayerGameTimeCapability;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunner;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.item.RMItems;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.game.*;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterCapabilitySyncS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterChangeS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerCapabilitySyncS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerChangeS2CPacket;
import net.laserdiamond.reversemanhunt.sound.RMSoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Events that are called when the {@linkplain RMGame.State Reverse Manhunt Game State} changes
 * <p>Events are fired on the {@linkplain net.minecraftforge.common.MinecraftForge#EVENT_BUS main Forge event bus}</p>
 */
public abstract class ReverseManhuntGameStateEvent extends Event {

    protected final List<Player> hunters;
    protected final List<Player> speedRunners;

    public ReverseManhuntGameStateEvent(List<Player> hunters, List<Player> speedRunners)
    {
        this.hunters = hunters;
        this.speedRunners = speedRunners;
        RMGame.setCurrentGameState(this.gameState()); // Set to the game state specified
        RMPackets.sendToAllClients(new GameStateS2CPacket(this.gameState())); // Send to all clients
    }

    /**
     * @return The {@linkplain RMGame.State game state} the Reverse Manhunt game will enter when the event is called
     */
    public abstract RMGame.State gameState();

    /**
     * @return A {@link List} of all the {@linkplain Player players} that are hunters
     */
    public List<Player> getHunters() {
        return hunters;
    }

    /**
     * @return A {@link List} of all the {@linkplain Player players} that are speed runners
     */
    public List<Player> getSpeedRunners() {
        return speedRunners;
    }

    /**
     * @return The current game time at which the event was called
     */
    public final long getGameTime()
    {
        return RMGame.getCurrentGameTime();
    }

    /**
     * Event called when the Revers Manhunt game starts
     */
    public static class Start extends ReverseManhuntGameStateEvent implements PlayerGameSpawner
    {
        public Start(List<Player> hunters, List<Player> speedRunners)
        {
            super(hunters, speedRunners);
            RMGame.resetGameTime(); // Reset the game time
            for (Player player : this.speedRunners)
            {
                RMGame.logPlayerUUID(player); // Log the player for this iteration of the game
                player.tickCount = 0; // Reset tick counts
                player.setHealth(player.getMaxHealth()); // Reset back to max health
                player.getInventory().clearContent(); // Clear items
                player.setItemSlot(EquipmentSlot.MAINHAND, RMItems.WIND_TORCH.get().getDefaultInstance()); // Give wind torch
                player.getFoodData().eat(200, 1.0F); // Reset food level

                if (!player.level().isClientSide) // Are we on the server?
                {
                    player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
                    {
                        playerSpeedRunner.setLives(PlayerSpeedRunner.getMaxLives()); // Reset lives
                        playerSpeedRunner.setWasLastKilledByHunter(false);
                        playerSpeedRunner.setGracePeriodTimeStamp(0);
                        RMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
                        RMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player.getId(), playerSpeedRunner.toNBT()), player);
                    });
                    player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                    {
                        // Speed runner is not a hunter or a buffed hunter
                        playerHunter.setHunter(false);
                        playerHunter.setBuffed(false);
                        RMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter), player);
                        RMPackets.sendToAllTrackingEntityAndSelf(new HunterCapabilitySyncS2CPacket(player.getId(), playerHunter.toNBT()), player);
                    });

                    this.spawn(player); // Move speed runners to spawn position
                }

            }
            for (Player player : this.hunters)
            {
                RMGame.logPlayerUUID(player); // Log the player for this iteration of the game
                player.tickCount = 0; // Reset tick counts
                player.setHealth(player.getMaxHealth()); // Reset back to max health
                player.getFoodData().eat(200, 1.0F); // Reset food level
                player.getInventory().clearContent(); // Clear items

                if (!player.level().isClientSide)
                {
                    player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
                    {
                        playerSpeedRunner.setLives(PlayerSpeedRunner.getMaxLives()); // Reset lives
                        playerSpeedRunner.setWasLastKilledByHunter(false);
                        playerSpeedRunner.setGracePeriodTimeStamp(0);
                        RMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
                        RMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player.getId(), playerSpeedRunner.toNBT()), player);
                    });
                    player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                    {
                        if (playerHunter.isHunter()) // Ensure that the player is a hunter
                        {
                            player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterSpawnAttributes()); // Add spawn attributes
                            if (playerHunter.isBuffed()) // Add buff attributes
                            {
                                player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterAttributes()); // Add buff attributes
                                player.setHealth(player.getMaxHealth());
                            }
                        }
                    });

                    MinecraftServer mcServer = player.getServer();
                    if (mcServer != null)
                    {
                        this.moveToOverworld(player, player.getServer()); // Move player to overworld
                    }
                }
            }

            RMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket(this.speedRunners.size(), this.hunters.size()));
        }

        @Override
        public RMGame.State gameState() {
            return RMGame.State.STARTED;
        }
    }

    /**
     * Event called when the Reverse Manhunt game ends
     */
    public static class End extends ReverseManhuntGameStateEvent
    {
        private final Reason reason;

        public End(Reason reason, List<Player> hunters, List<Player> speedRunners)
        {
            super(hunters, speedRunners);
            this.reason = reason;
            RMPackets.sendToAllClients(new GameEndAnnounceS2CPacket(this.reason));
            RMPackets.sendToAllClients(new GameTimeS2CPacket(0)); // Reset Game Time
            RMGame.wipeLoggedPlayerUUIDs(); // Wipe the logged players
            for (Player player : this.hunters)
            {
                RMSoundEvents.stopDetectionSound(player);
                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                {
                    player.getAttributes().removeAttributeModifiers(PlayerHunter.createHunterSpawnAttributes()); // Remove spawn attributes
                    if (playerHunter.isBuffed()) // Check if buffed
                    {
                        player.getAttributes().removeAttributeModifiers(PlayerHunter.createHunterAttributes()); // Remove buffed attributes
                    }
                    // Reset all players from being a hunter
                    playerHunter.setHunter(false);
                    playerHunter.setBuffed(false);
                    RMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter), player);
                    RMPackets.sendToAllTrackingEntityAndSelf(new HunterCapabilitySyncS2CPacket(player.getId(), playerHunter.toNBT()), player);
                });
                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
                {
                    playerSpeedRunner.setLives(PlayerSpeedRunner.getMaxLives());
                    playerSpeedRunner.setWasLastKilledByHunter(false);
                    playerSpeedRunner.setGracePeriodTimeStamp(0);
                    RMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
                    RMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player.getId(), playerSpeedRunner.toNBT()), player);
                });
                player.getCapability(PlayerGameTimeCapability.PLAYER_GAME_TIME).ifPresent(playerGameTime ->
                {
                    playerGameTime.setGameTime(0);
                    RMPackets.sendToAllTrackingEntityAndSelf(new GameTimeCapabilitySyncS2CPacket(player.getId(), playerGameTime.toNBT()), player);
                });
            }
            for (Player player : this.speedRunners)
            {
                RMSoundEvents.stopDetectionSound(player);
                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
                {
                    playerSpeedRunner.setLives(PlayerSpeedRunner.getMaxLives());
                    playerSpeedRunner.setWasLastKilledByHunter(false);
                    playerSpeedRunner.setGracePeriodTimeStamp(0);
                    RMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
                    RMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player.getId(), playerSpeedRunner.toNBT()), player);
                });
                player.getCapability(PlayerGameTimeCapability.PLAYER_GAME_TIME).ifPresent(playerGameTime ->
                {
                    playerGameTime.setGameTime(0);
                    RMPackets.sendToAllTrackingEntityAndSelf(new GameTimeCapabilitySyncS2CPacket(player.getId(), playerGameTime.toNBT()), player);
                });
            }
        }

        public Reason getReason() {
            return reason;
        }

        @Override
        public RMGame.State gameState() {
            return RMGame.State.NOT_STARTED;
        }

        public enum Reason
        {
            /**
             * The Hunters have won the game
             */
            HUNTER_WIN,

            /**
             * The remaining speed runners have won the game
             */
            SPEED_RUNNERS_WIN,

            /**
             * The game was ended using the {@linkplain net.laserdiamond.reversemanhunt.commands.ReverseManhuntGameCommands Reverse Manhunt Game Command}
             */
            COMMAND;

            public static Reason fromOrdinal(int value)
            {
                for (Reason reason : values())
                {
                    if (reason.ordinal() == value)
                    {
                        return reason;
                    }
                }
                return null;
            }
        }
    }

    /**
     * Event called when the {@linkplain RMGame Reverse Manhunt game state} has been set to {@linkplain RMGame.State#PAUSED paused}
     */
    public static class Pause extends ReverseManhuntGameStateEvent
    {

        public Pause(List<Player> hunters, List<Player> speedRunners)
        {
            super(hunters, speedRunners);
            for (Player player : hunters)
            {
                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                {
                    if (playerHunter.isHunter())
                    {
                        player.getAttributes().removeAttributeModifiers(PlayerHunter.createHunterSpawnAttributes()); // Remove spawn attributes from hunters
                        if (playerHunter.isBuffed())
                        {
                            player.getAttributes().removeAttributeModifiers(PlayerHunter.createHunterAttributes()); // Remove buff attributes from hunter
                        }
                    }
                });
            }
        }

        @Override
        public RMGame.State gameState() {
            return RMGame.State.PAUSED;
        }
    }

    /**
     * Event called when the {@linkplain RMGame Reverse Manhunt game state} has been set to {@linkplain RMGame.State#IN_PROGRESS in progress}
     */
    public static class Resume extends ReverseManhuntGameStateEvent
    {

        public Resume(List<Player> hunters, List<Player> speedRunners)
        {
            super(hunters, speedRunners);
            for (Player player : hunters)
            {
                if (!RMGame.containsLoggedPlayerUUID(player)) // Check if we have this player logged
                {
                    RMGame.logPlayerUUID(player); // Log them
                }
                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                {
                    if (playerHunter.isHunter()) // Ensure that the player is a hunter
                    {
                        if (RMGame.areHuntersOnGracePeriod()) // Is the game still on grace period?
                        {
                            player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterSpawnAttributes());
                        }
                        if (playerHunter.isBuffed()) // Is the player a buffed hunter?
                        {
                            player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterAttributes()); // Add buff attributes
                        }
                    }
                });
            }
            for (Player player : speedRunners)
            {
                if (!RMGame.containsLoggedPlayerUUID(player))
                {
                    RMGame.logPlayerUUID(player);
                }
            }
        }

        @Override
        public RMGame.State gameState() {
            return RMGame.State.IN_PROGRESS;
        }
    }
}
