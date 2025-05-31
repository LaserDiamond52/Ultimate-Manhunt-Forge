package net.laserdiamond.ultimatemanhunt.api.event;

import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.item.UMItems;
import net.laserdiamond.ultimatemanhunt.item.WindTorchItem;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.game.*;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GameEndAnnounceS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GamePausedAnnounceS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GameResumedS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GameStartAnnounceS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerDistanceFromHunterS2CPacket;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.LinkedList;
import java.util.List;

/**
 * Events that are called when the {@linkplain UMGame.State Ultimate Manhunt Game State} changes
 * <p>Events are fired on the {@linkplain net.minecraftforge.common.MinecraftForge#EVENT_BUS main Forge event bus}</p>
 */
public abstract class UltimateManhuntGameStateEvent extends Event {

    protected final List<Player> hunters;
    protected final List<Player> speedRunners;
    protected final List<Player> spectators;

    public UltimateManhuntGameStateEvent()
    {
        this.hunters = new LinkedList<>();
        this.speedRunners = new LinkedList<>();
        this.spectators = new LinkedList<>();

        UMPlayer.forAllPlayers(
                (player, umPlayer) -> {
                    this.speedRunners.add(player);
                    this.forSpeedRunner(player, umPlayer);
                },
                (player, umPlayer) -> {
                    this.hunters.add(player);
                    this.forHunter(player, umPlayer);
                },
                (player, umPlayer) -> {
                    this.spectators.add(player);
                    this.forSpectators(player, umPlayer);
                },
                this::forAllPlayers
        );
        UMGame.setCurrentGameState(this.gameState()); // Set to the game state specified
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
     * Called for all players
     * @param player The {@linkplain Player player} being called upon
     * @param umPlayer
     */
    protected void forAllPlayers(Player player, UMPlayer umPlayer) {}

    /**
     * Called for each Player Speed Runner
     * @param player The {@linkplain Player player} that is a speed runner
     */
    protected void forSpeedRunner(Player player, UMPlayer umPlayer) {}

    /**
     * Called for each Player Hunter
     * @param player The {@linkplain Player player} that is a hunter
     */
    protected void forHunter(Player player, UMPlayer umPlayer) {}

    /**
     * Called for each Player Spectator
     * @param player The {@linkplain Player player} that is a spectator
     */
    protected void forSpectators(Player player, UMPlayer umPlayer) {}

    /**
     * Event called when the Revers Manhunt game starts
     */
    public static class Start extends UltimateManhuntGameStateEvent implements PlayerGameSpawner
    {
        public Start()
        {
            super();
            UMGame.resetGameTime(); // Reset the game time
            UMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket());
            UMPackets.sendToAllClients(new GameStartAnnounceS2CPacket());
        }

        @Override
        public UMGame.State gameState() {
            return UMGame.State.STARTED;
        }

        @Override
        protected void forAllPlayers(Player player, UMPlayer umPlayer)
        {
            player.setHealth(player.getMaxHealth());
            player.tickCount = 0; // Reset tick counts
            player.getFoodData().eat(200, 1.0F); // Reset food level
        }

        @Override
        protected void forSpeedRunner(Player player, UMPlayer umPlayer)
        {
            if (!player.level().isClientSide) // Are we on the server?
            {
//                UMGame.logPlayerUUID(player); // Log the player for this iteration of the game
                player.getInventory().clearContent(); // Clear items
                if (UMGame.isWindTorchEnabled()) // Check if Wind Torches are enabled
                {
                    player.setItemSlot(EquipmentSlot.MAINHAND, UMItems.WIND_TORCH.get().getDefaultInstance()); // Give wind torch
                }

                umPlayer.resetToSpeedRunner(player, true); // Reset back to speed runner

                this.spawn(player); // Move speed runners to spawn position
            }
        }

        @Override
        protected void forHunter(Player player, UMPlayer umPlayer)
        {
            if (!player.level().isClientSide)
            {
//                UMGame.logPlayerUUID(player); // Log the player for this iteration of the game
                player.getInventory().clearContent(); // Clear items

//                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
//                {
//                    playerSpeedRunner.setLives(PlayerSpeedRunner.getMaxLives()); // Reset lives
//                    playerSpeedRunner.setWasLastKilledByHunter(false);
//                    playerSpeedRunner.setGracePeriodTimeStamp(0);
//                    UMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
//                    UMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player, playerSpeedRunner), player);
//                });
//                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
//                {
//                    if (playerHunter.isHunter()) // Ensure that the player is a hunter
//                    {
//                        player.getAbilities().mayfly = true;
//                        player.getAbilities().flying = true;
//                        player.onUpdateAbilities();
//                        player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterSpawnAttributes()); // Add spawn attributes
//                        if (playerHunter.isBuffed()) // Add buff attributes
//                        {
//                            player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterAttributes()); // Add buff attributes
//                            player.setHealth(player.getMaxHealth());
//                        }
//                    }
//                });

                umPlayer.resetToHunter(player, true);
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
                if (umPlayer.isBuffedHunter())
                {
                    player.getAttributes().addTransientAttributeModifiers(UMPlayer.createHunterAttributes()); // Add buff attributes
                    player.setHealth(player.getMaxHealth());
                }

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

        public End(Reason reason)
        {
            super();
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
        protected void forAllPlayers(Player player, UMPlayer umPlayer)
        {
            UMSoundEvents.stopDetectionSound(player);
        }

        @Override
        protected void forSpeedRunner(Player player, UMPlayer umPlayer)
        {
            if (!player.level().isClientSide)
            {
//                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
//                {
//                    playerSpeedRunner.setLives(PlayerSpeedRunner.getMaxLives());
//                    playerSpeedRunner.setWasLastKilledByHunter(false);
//                    playerSpeedRunner.setGracePeriodTimeStamp(0);
//                    UMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
//                    UMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player, playerSpeedRunner), player);
//                });
//                player.getCapability(PlayerGameTimeCapability.PLAYER_GAME_TIME).ifPresent(playerGameTime ->
//                {
//                    playerGameTime.setGameTime(0);
//                    UMPackets.sendToAllTrackingEntityAndSelf(new GameTimeCapabilitySyncS2CPacket(player, playerGameTime), player);
//                });

                umPlayer.resetToSpeedRunner(player, false);

                player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.getItem() instanceof WindTorchItem, -1, player.inventoryMenu.getCraftSlots());
            }
        }

        @Override
        protected void forHunter(Player player, UMPlayer umPlayer)
        {
            if (!player.level().isClientSide)
            {
//                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
//                {
//                    player.getAttributes().removeAttributeModifiers(PlayerHunter.createHunterSpawnAttributes()); // Remove spawn attributes
//                    if (playerHunter.isBuffed()) // Check if buffed
//                    {
//                        player.getAttributes().removeAttributeModifiers(PlayerHunter.createHunterAttributes()); // Remove buffed attributes
//                    }
//                    // Reset all players from being a hunter
//                    playerHunter.setHunter(false);
//                    playerHunter.setBuffed(false);
//                    UMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter), player);
//                    UMPackets.sendToAllTrackingEntityAndSelf(new HunterCapabilitySyncS2CPacket(player, playerHunter), player);
//                    if (this.getGameTime() < UMGame.getHunterGracePeriod()) // Are hunters still on grace period?
//                    {
//                        this.spawn(player); // Teleport hunter back down
//
//                        // This is necessary since hunters are teleported 1000 blocks in the air and will be kicked for flying if
//                        // teleportation back to the surface is not performed
//                    }
//                });
//                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
//                {
//                    playerSpeedRunner.setLives(PlayerSpeedRunner.getMaxLives());
//                    playerSpeedRunner.setWasLastKilledByHunter(false);
//                    playerSpeedRunner.setGracePeriodTimeStamp(0);
//                    UMPackets.sendToPlayer(new SpeedRunnerChangeS2CPacket(playerSpeedRunner), player);
//                    UMPackets.sendToAllTrackingEntityAndSelf(new SpeedRunnerCapabilitySyncS2CPacket(player, playerSpeedRunner), player);
//                });
//                player.getCapability(PlayerGameTimeCapability.PLAYER_GAME_TIME).ifPresent(playerGameTime ->
//                {
//                    playerGameTime.setGameTime(0);
//                    UMPackets.sendToAllTrackingEntityAndSelf(new GameTimeCapabilitySyncS2CPacket(player, playerGameTime), player);
//                });

                if (umPlayer.isBuffedHunter())
                {
                    player.getAttributes().removeAttributeModifiers(UMPlayer.createHunterAttributes()); // Add buff attributes
                }
                umPlayer.resetToSpeedRunner(player, false);
                if (this.getGameTime() < UMGame.getHunterGracePeriod())
                {
                    this.spawn(player); // Teleport hunter back down
                    // This is necessary since hunters are teleported 1000 blocks in the air and will be kicked for flying if
                    // teleportation back to the surface is not performed
                }
                player.getAbilities().mayfly = (player.isCreative() || player.isSpectator());
                player.getAbilities().flying = (player.isCreative() || player.isSpectator()) && player.getAbilities().flying;
                player.onUpdateAbilities();

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
             * The game was ended using the {@linkplain net.laserdiamond.ultimatemanhunt.commands.sub.SetGameStateSC Ultimate Manhunt Game Command}
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

        public Pause()
        {
            super();
            UMPackets.sendToAllClients(new GamePausedAnnounceS2CPacket());
            UMPackets.sendToAllClients(new SpeedRunnerDistanceFromHunterS2CPacket(0));
        }

        @Override
        public UMGame.State gameState() {
            return UMGame.State.PAUSED;
        }

        @Override
        protected void forHunter(Player player, UMPlayer umPlayer)
        {
            if (!player.level().isClientSide)
            {
//                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
//                {
//                    if (playerHunter.isHunter())
//                    {
//                        player.getAttributes().removeAttributeModifiers(PlayerHunter.createHunterSpawnAttributes()); // Remove spawn attributes from hunters
//                        if (playerHunter.isBuffed())
//                        {
//                            player.getAttributes().removeAttributeModifiers(PlayerHunter.createHunterAttributes()); // Remove buff attributes from hunter
//                        }
//                    }
//                });

                if (umPlayer.isBuffedHunter())
                {
                    player.getAttributes().removeAttributeModifiers(UMPlayer.createHunterAttributes());
                }
            }
        }
    }

    /**
     * Event called when the {@linkplain UMGame Ultimate Manhunt game state} has been set to {@linkplain UMGame.State#IN_PROGRESS in progress}
     */
    public static class Resume extends UltimateManhuntGameStateEvent
    {

        public Resume()
        {
            super();
            UMPackets.sendToAllClients(new GameResumedS2CPacket());
            UMPackets.sendToAllClients(new SpeedRunnerDistanceFromHunterS2CPacket(0));
            UMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket());
        }

        @Override
        public UMGame.State gameState() {
            return UMGame.State.IN_PROGRESS;
        }

        @Override
        protected void forSpeedRunner(Player player, UMPlayer umPlayer)
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
        protected void forHunter(Player player, UMPlayer umPlayer)
        {
            if (!player.level().isClientSide)
            {
                if (!UMGame.containsLoggedPlayerUUID(player)) // Check if we have this player logged
                {
                    UMGame.logPlayerUUID(player); // Log them
                }
//                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
//                {
//                    if (playerHunter.isHunter()) // Ensure that the player is a hunter
//                    {
//                        if (UMGame.areHuntersOnGracePeriod()) // Is the game still on grace period?
//                        {
//                            player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterSpawnAttributes());
//                        }
//                        if (playerHunter.isBuffed()) // Is the player a buffed hunter?
//                        {
//                            player.getAttributes().addTransientAttributeModifiers(PlayerHunter.createHunterAttributes()); // Add buff attributes
//                        }
//                    }
//                });

                if (umPlayer.isBuffedHunter())
                {
                    player.getAttributes().addTransientAttributeModifiers(UMPlayer.createHunterAttributes()); // Add buff attributes
                }
            }
        }
    }
}
