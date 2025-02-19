package net.laserdiamond.reversemanhunt.api;

import net.laserdiamond.reversemanhunt.RMGameState;
import net.laserdiamond.reversemanhunt.capability.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.capability.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.item.RMItems;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.game.GameEndAnnounceS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.game.GameStateS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterChangeS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.HunterDetectionS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerLifeChangeS2CPacket;
import net.laserdiamond.reversemanhunt.sound.RMSoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Events that are called when the {@linkplain RMGameState.State Reverse Manhunt Game State} changes
 * <p>Events are fired on the {@linkplain net.minecraftforge.common.MinecraftForge#EVENT_BUS main Forge event bus}</p>
 */
public abstract class ReverseManhuntGameStateEvent extends Event {

    protected final List<Player> hunters;
    protected final List<Player> speedRunners;

    public ReverseManhuntGameStateEvent(List<Player> hunters, List<Player> speedRunners)
    {
        this.hunters = hunters;
        this.speedRunners = speedRunners;
        RMGameState.setCurrentGameState(this.gameState()); // Set to the game state specified
        RMPackets.sendToAllClients(new GameStateS2CPacket(this.gameState())); // Send to all clients
    }

    /**
     * @return The {@linkplain net.laserdiamond.reversemanhunt.RMGameState.State game state} the Reverse Manhunt game will enter when the event is called
     */
    public abstract RMGameState.State gameState();

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
        return RMGameState.getCurrentGameTime();
    }

    /**
     * Event called when the Revers Manhunt game starts
     */
    public static class Start extends ReverseManhuntGameStateEvent
    {

        public Start(List<Player> hunters, List<Player> speedRunners) throws UnsupportedOperationException
        {
            super(hunters, speedRunners);
            RMGameState.resetGameTime(); // Reset the game time
            for (Player player : this.speedRunners)
            {
                RMGameState.logPlayerUUID(player); // Log the player for this iteration of the game
                player.tickCount = 0; // Reset tick counts
                player.setHealth(player.getMaxHealth()); // Reset back to max health
                player.getInventory().clearContent(); // Clear items
                player.setItemSlot(EquipmentSlot.MAINHAND, RMItems.WIND_TORCH.get().getDefaultInstance()); // Give wind torch
                player.getFoodData().eat(200, 1.0F); // Reset food level

                if (!player.level().isClientSide) // Are we on the server?
                {
                    player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
                    {
                        playerSpeedRunner.setLives(RMGameState.SPEED_RUNNER_LIVES); // Reset lives
                        playerSpeedRunner.setWasLastKilledByHunter(false);
                        RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(playerSpeedRunner), player);
                    });
                    player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                    {
                        // Speed runner is not a hunter or a buffed hunter
                        playerHunter.setHunter(false);
                        playerHunter.setBuffed(false);
                        RMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter), player);
                    });
                }

            }
            for (Player player : this.hunters)
            {
                RMGameState.logPlayerUUID(player); // Log the player for this iteration of the game
                player.tickCount = 0; // Reset tick counts
                player.setHealth(player.getMaxHealth()); // Reset back to max health
                player.getFoodData().eat(200, 1.0F); // Reset food level

                if (!player.level().isClientSide)
                {
                    player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
                    {
                        playerSpeedRunner.setLives(RMGameState.SPEED_RUNNER_LIVES); // Reset lives
                        playerSpeedRunner.setWasLastKilledByHunter(false);
                        RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(playerSpeedRunner), player);
                    });
                    player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                    {
                        if (playerHunter.isHunter()) // Ensure that the player is a hunter
                        {
                            player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterSpawnAttributes()); // Add spawn attributes
                            if (playerHunter.isBuffed()) // Add buff attributes
                            {
                                player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterAttributes()); // Add buff attributes
                            }
                        }
                    });
                }
            }
        }

        @Override
        public RMGameState.State gameState() {
            return RMGameState.State.STARTED;
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
            RMPackets.sendToAllClients(new HunterDetectionS2CPacket(false));
            RMGameState.wipeLoggedPlayerUUIDs(); // Wipe the logged players
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
                });
                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
                {
                    playerSpeedRunner.setLives(RMGameState.SPEED_RUNNER_LIVES);
                    playerSpeedRunner.setWasLastKilledByHunter(false);
                    RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(playerSpeedRunner), player);
                });
            }
            for (Player player : this.speedRunners)
            {
                RMSoundEvents.stopDetectionSound(player);
                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
                {
                    playerSpeedRunner.setLives(RMGameState.SPEED_RUNNER_LIVES);
                    playerSpeedRunner.setWasLastKilledByHunter(false);
                    RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(playerSpeedRunner), player);
                });
            }
        }

        public Reason getReason() {
            return reason;
        }

        @Override
        public RMGameState.State gameState() {
            return RMGameState.State.NOT_STARTED;
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
     * Event called when the {@linkplain RMGameState Reverse Manhunt game state} has been set to {@linkplain net.laserdiamond.reversemanhunt.RMGameState.State#PAUSED paused}
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
        public RMGameState.State gameState() {
            return RMGameState.State.PAUSED;
        }
    }

    /**
     * Event called when the {@linkplain RMGameState Reverse Manhunt game state} has been set to {@linkplain net.laserdiamond.reversemanhunt.RMGameState.State#IN_PROGRESS in progress}
     */
    public static class Resume extends ReverseManhuntGameStateEvent
    {

        public Resume(List<Player> hunters, List<Player> speedRunners)
        {
            super(hunters, speedRunners);
            for (Player player : hunters)
            {
                if (!RMGameState.containsLoggedPlayerUUID(player)) // Check if we have this player logged
                {
                    RMGameState.logPlayerUUID(player); // Log them
                }
                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                {
                    if (playerHunter.isHunter()) // Ensure that the player is a hunter
                    {
                        if (RMGameState.areHuntersOnGracePeriod()) // Is the game still on grace period?
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
                if (!RMGameState.containsLoggedPlayerUUID(player))
                {
                    RMGameState.logPlayerUUID(player);
                }
            }
        }

        @Override
        public RMGameState.State gameState() {
            return RMGameState.State.IN_PROGRESS;
        }
    }
}
