package net.laserdiamond.ultimatemanhunt.api.event;

import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.item.UMItems;
import net.laserdiamond.ultimatemanhunt.item.WindTorchItem;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GameEndAnnounceS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.GameTimeS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.RemainingPlayerCountS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GamePausedAnnounceS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GameResumedS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GameStartAnnounceS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerDistanceFromHunterS2CPacket;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
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
     * @param umPlayer The {@linkplain Player player's} {@linkplain UMPlayer Manhunt player data}
     */
    protected void forAllPlayers(Player player, UMPlayer umPlayer) {}

    /**
     * Called for each Player Speed Runner
     * @param player The {@linkplain Player player} that is a speed runner
     * @param umPlayer The {@linkplain Player player's} {@linkplain UMPlayer Manhunt player data}
     */
    protected void forSpeedRunner(Player player, UMPlayer umPlayer) {}

    /**
     * Called for each Player Hunter
     * @param player The {@linkplain Player player} that is a hunter
     * @param umPlayer The {@linkplain Player player's} {@linkplain UMPlayer Manhunt player data}
     */
    protected void forHunter(Player player, UMPlayer umPlayer) {}

    /**
     * Called for each Player Spectator
     * @param player The {@linkplain Player player} that is a spectator
     * @param umPlayer The {@linkplain Player player's} {@linkplain UMPlayer Manhunt player data}
     */
    protected void forSpectators(Player player, UMPlayer umPlayer) {}

    /**
     * Event called when the Manhunt game starts
     */
    public static class Start extends UltimateManhuntGameStateEvent implements PlayerGameSpawner
    {
        public Start()
        {
            super();
            UMGame.resetGameTime(); // Reset the game time
            UMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket());
            UMPackets.sendToAllClients(new GameStartAnnounceS2CPacket());
            SpeedRunnerDistanceFromHunterS2CPacket.sendNotNearHunterAll();
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
            UMSoundEvents.stopDetectionSound(player);
            UMSoundEvents.stopFlatlineSound(player);
            player.getInventory().clearContent(); // Clear items
            if (player.isSpectator())
            {
                umPlayer.setRole(UMGame.PlayerRole.SPECTATOR); // If the player is in spectator mode, set them to be a spectator
            }
            if (!umPlayer.isSpectator()) // Is the player not declared a spectator?
            {
                if (player instanceof ServerPlayer serverPlayer)
                {
                    serverPlayer.setGameMode(GameType.DEFAULT_MODE); // Set to survival
                }
            }
        }

        @Override
        protected void forSpeedRunner(Player player, UMPlayer umPlayer)
        {
            if (!player.level().isClientSide) // Are we on the server?
            {
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
                // Log the player for this iteration of the game
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
                umPlayer.resetToSpeedRunner(player, false);

                player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.getItem() instanceof WindTorchItem, -1, player.inventoryMenu.getCraftSlots());
            }
        }

        @Override
        protected void forHunter(Player player, UMPlayer umPlayer)
        {
            if (!player.level().isClientSide)
            {
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
             * The game was ended using the {@linkplain net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands Ultimate Manhunt Game Command}
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
            SpeedRunnerDistanceFromHunterS2CPacket.sendNotNearHunterAll();
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
            UMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket());
            SpeedRunnerDistanceFromHunterS2CPacket.sendNotNearHunterAll();
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
                if (umPlayer.isBuffedHunter())
                {
                    player.getAttributes().addTransientAttributeModifiers(UMPlayer.createHunterAttributes()); // Add buff attributes
                }
            }
        }
    }
}
