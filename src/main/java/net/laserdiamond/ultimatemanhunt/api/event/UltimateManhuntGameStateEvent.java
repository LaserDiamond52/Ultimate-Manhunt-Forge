package net.laserdiamond.ultimatemanhunt.api.event;

import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.capability.game.PlayerGameTimeCapability;
import net.laserdiamond.ultimatemanhunt.capability.hunter.PlayerHunter;
import net.laserdiamond.ultimatemanhunt.capability.hunter.PlayerHunterCapability;
import net.laserdiamond.ultimatemanhunt.capability.speedrunner.PlayerSpeedRunner;
import net.laserdiamond.ultimatemanhunt.capability.speedrunner.PlayerSpeedRunnerCapability;
import net.laserdiamond.ultimatemanhunt.item.UMItems;
import net.laserdiamond.ultimatemanhunt.item.WindTorchItem;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.game.*;
import net.laserdiamond.ultimatemanhunt.network.packet.hunter.HunterCapabilitySyncS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.hunter.HunterChangeS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerCapabilitySyncS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerChangeS2CPacket;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Events that are called when the {@linkplain UMGame.State Ultimate Manhunt Game State} changes
 * <p>Events are fired on the {@linkplain net.minecraftforge.common.MinecraftForge#EVENT_BUS main Forge event bus}</p>
 */
public abstract class UltimateManhuntGameStateEvent extends Event {

    protected final List<Player> hunters;
    protected final List<Player> speedRunners;

    public UltimateManhuntGameStateEvent(List<Player> hunters, List<Player> speedRunners)
    {
        this.hunters = hunters;
        this.speedRunners = speedRunners;
        UMGame.setCurrentGameState(this.gameState()); // Set to the game state specified
        UMPackets.sendToAllClients(new GameStateS2CPacket(this.gameState())); // Send to all clients
        this.speedRunners.forEach(this::forSpeedRunner);
        this.hunters.forEach(this::forHunter);
    }

    /**
     * @return The {@linkplain UMGame.State game state} the Ultimate Manhunt game will enter when the event is called
     */
    public abstract UMGame.State gameState();

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
        return UMGame.getCurrentGameTime();
    }

    /**
     * Called for each Player Speed Runner
     * @param player The {@linkplain Player player} that is a speed runner
     */
    protected abstract void forSpeedRunner(Player player);

    /**
     * Called for each Player Hunter
     * @param player The {@linkplain Player player} that is a hunter
     */
    protected abstract void forHunter(Player player);

    /**
     * Event called when the Revers Manhunt game starts
     */
    public static class Start extends UltimateManhuntGameStateEvent implements PlayerGameSpawner
    {
        public Start(List<Player> hunters, List<Player> speedRunners)
        {
            super(hunters, speedRunners);
            UMGame.resetGameTime(); // Reset the game time
            UMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket(this.speedRunners.size(), this.hunters.size()));
        }

        @Override
        public UMGame.State gameState() {
            return UMGame.State.STARTED;
        }

        @Override
        protected void forSpeedRunner(Player player)
        {
            if (!player.level().isClientSide) // Are we on the server?
            {
                UMGame.logPlayerUUID(player); // Log the player for this iteration of the game
                player.tickCount = 0; // Reset tick counts
                player.setHealth(player.getMaxHealth()); // Reset back to max health
                player.getInventory().clearContent(); // Clear items
                if (UMGame.isWindTorchEnabled()) // Check if Wind Torches are enabled
                {
                    player.setItemSlot(EquipmentSlot.MAINHAND, UMItems.WIND_TORCH.get().getDefaultInstance()); // Give wind torch
                }
                player.getFoodData().eat(200, 1.0F); // Reset food level

                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
                {
                    playerSpeedRunner.setLives(PlayerSpeedRunner.getMaxLives()); // Reset lives
                    playerSpeedRunner.setWasLastKilledByHunter(false);
                    playerSpeedRunner.setGracePeriodTimeStamp(0);
                    UMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
                    UMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player.getId(), playerSpeedRunner.toNBT()), player);
                });
                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                {
                    // Speed runner is not a hunter or a buffed hunter
                    playerHunter.setHunter(false);
                    playerHunter.setBuffed(false);
                    UMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter), player);
                    UMPackets.sendToAllTrackingEntityAndSelf(new HunterCapabilitySyncS2CPacket(player.getId(), playerHunter.toNBT()), player);
                });

                this.spawn(player); // Move speed runners to spawn position
            }
        }

        @Override
        protected void forHunter(Player player)
        {
            if (!player.level().isClientSide)
            {
                UMGame.logPlayerUUID(player); // Log the player for this iteration of the game
                player.tickCount = 0; // Reset tick counts
                player.setHealth(player.getMaxHealth()); // Reset back to max health
                player.getFoodData().eat(200, 1.0F); // Reset food level
                player.getInventory().clearContent(); // Clear items

                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
                {
                    playerSpeedRunner.setLives(PlayerSpeedRunner.getMaxLives()); // Reset lives
                    playerSpeedRunner.setWasLastKilledByHunter(false);
                    playerSpeedRunner.setGracePeriodTimeStamp(0);
                    UMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
                    UMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player.getId(), playerSpeedRunner.toNBT()), player);
                });
                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                {
                    if (playerHunter.isHunter()) // Ensure that the player is a hunter
                    {
                        player.getAbilities().mayfly = true;
                        player.getAbilities().flying = true;
                        player.onUpdateAbilities();
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
    }

    /**
     * Event called when the Ultimate Manhunt game ends
     */
    public static class End extends UltimateManhuntGameStateEvent implements PlayerGameSpawner
    {
        private final Reason reason;

        public End(Reason reason, List<Player> hunters, List<Player> speedRunners)
        {
            super(hunters, speedRunners);
            this.reason = reason;
            UMPackets.sendToAllClients(new GameEndAnnounceS2CPacket(this.reason));
            UMPackets.sendToAllClients(new GameTimeS2CPacket(0)); // Reset Game Time
            UMGame.wipeLoggedPlayerUUIDs(); // Wipe the logged players
        }

        public Reason getReason() {
            return reason;
        }

        @Override
        public UMGame.State gameState() {
            return UMGame.State.NOT_STARTED;
        }

        @Override
        protected void forSpeedRunner(Player player)
        {
            if (!player.level().isClientSide)
            {
                UMSoundEvents.stopDetectionSound(player);
                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
                {
                    playerSpeedRunner.setLives(PlayerSpeedRunner.getMaxLives());
                    playerSpeedRunner.setWasLastKilledByHunter(false);
                    playerSpeedRunner.setGracePeriodTimeStamp(0);
                    UMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
                    UMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player.getId(), playerSpeedRunner.toNBT()), player);
                });
                player.getCapability(PlayerGameTimeCapability.PLAYER_GAME_TIME).ifPresent(playerGameTime ->
                {
                    playerGameTime.setGameTime(0);
                    UMPackets.sendToAllTrackingEntityAndSelf(new GameTimeCapabilitySyncS2CPacket(player.getId(), playerGameTime.toNBT()), player);
                });
                player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.getItem() instanceof WindTorchItem, -1, player.inventoryMenu.getCraftSlots());
            }
        }

        @Override
        protected void forHunter(Player player)
        {
            if (!player.level().isClientSide)
            {
                UMSoundEvents.stopDetectionSound(player);
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
                    UMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter), player);
                    UMPackets.sendToAllTrackingEntityAndSelf(new HunterCapabilitySyncS2CPacket(player.getId(), playerHunter.toNBT()), player);
                    if (this.getGameTime() < UMGame.getHunterGracePeriod()) // Are hunters still on grace period?
                    {
                        this.spawn(player); // Teleport hunter back down

                        // This is necessary since hunters are teleported 1000 blocks in the air and will be kicked for flying if
                        // teleportation back to the surface is not performed
                    }
                });
                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
                {
                    playerSpeedRunner.setLives(PlayerSpeedRunner.getMaxLives());
                    playerSpeedRunner.setWasLastKilledByHunter(false);
                    playerSpeedRunner.setGracePeriodTimeStamp(0);
                    UMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
                    UMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player.getId(), playerSpeedRunner.toNBT()), player);
                });
                player.getCapability(PlayerGameTimeCapability.PLAYER_GAME_TIME).ifPresent(playerGameTime ->
                {
                    playerGameTime.setGameTime(0);
                    UMPackets.sendToAllTrackingEntityAndSelf(new GameTimeCapabilitySyncS2CPacket(player.getId(), playerGameTime.toNBT()), player);
                });
                player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.getItem() instanceof WindTorchItem, -1, player.inventoryMenu.getCraftSlots());
            }
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
             * The game was ended using the {@linkplain net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntGameCommands Ultimate Manhunt Game Command}
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
     * Event called when the {@linkplain UMGame Ultimate Manhunt game state} has been set to {@linkplain UMGame.State#PAUSED paused}
     */
    public static class Pause extends UltimateManhuntGameStateEvent
    {

        public Pause(List<Player> hunters, List<Player> speedRunners)
        {
            super(hunters, speedRunners);
        }

        @Override
        public UMGame.State gameState() {
            return UMGame.State.PAUSED;
        }

        @Override
        protected void forSpeedRunner(Player player) {

        }

        @Override
        protected void forHunter(Player player)
        {
            if (!player.level().isClientSide)
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
    }

    /**
     * Event called when the {@linkplain UMGame Ultimate Manhunt game state} has been set to {@linkplain UMGame.State#IN_PROGRESS in progress}
     */
    public static class Resume extends UltimateManhuntGameStateEvent
    {

        public Resume(List<Player> hunters, List<Player> speedRunners)
        {
            super(hunters, speedRunners);
        }

        @Override
        public UMGame.State gameState() {
            return UMGame.State.IN_PROGRESS;
        }

        @Override
        protected void forSpeedRunner(Player player)
        {
            if (!player.level().isClientSide)
            {
                if (!UMGame.containsLoggedPlayerUUID(player))
                {
                    UMGame.logPlayerUUID(player);
                }
            }
        }

        @Override
        protected void forHunter(Player player)
        {
            if (!player.level().isClientSide)
            {
                if (!UMGame.containsLoggedPlayerUUID(player)) // Check if we have this player logged
                {
                    UMGame.logPlayerUUID(player); // Log them
                }
                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                {
                    if (playerHunter.isHunter()) // Ensure that the player is a hunter
                    {
                        if (UMGame.areHuntersOnGracePeriod()) // Is the game still on grace period?
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
        }
    }
}
