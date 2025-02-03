package net.laserdiamond.reversemanhunt.capability;

import net.laserdiamond.reversemanhunt.RMGameState;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerLifeChangeC2SPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerLifeChangeS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegisterCapability
public class PlayerSpeedRunner {

    public static final int MIN_LIVES = 0; // Minimum amount of lives a speed runner can have
    public static final boolean BUFFED_HUNTER_ON_FINAL_DEATH = false; // Does the speed runner become a buffed hunter after losing their final life?

    public static void setCapabilitySpeedRunnerValues(Player player, int lifeCount, boolean wasLastKilledByHunter, @NotNull LogicalSide fromSide)
    {
        player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
        {
            playerSpeedRunner.setLives(lifeCount);
            playerSpeedRunner.setWasLastKilledByHunter(wasLastKilledByHunter);
            switch (fromSide)
            {
                case CLIENT -> RMPackets.sendToServer(new SpeedRunnerLifeChangeC2SPacket(lifeCount, wasLastKilledByHunter));
                case SERVER -> RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(lifeCount, wasLastKilledByHunter), player);
            }
        });
    }

    public static void removeSpeedRunnerLife(Player player, boolean wasLastKilledByHunter, @NotNull LogicalSide fromSide)
    {
        player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
        {
            playerSpeedRunner.subtractLife();
            playerSpeedRunner.setWasLastKilledByHunter(wasLastKilledByHunter);
            switch (fromSide)
            {
                case CLIENT -> RMPackets.sendToServer(new SpeedRunnerLifeChangeC2SPacket(playerSpeedRunner.getLives(), wasLastKilledByHunter));
                case SERVER -> RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(playerSpeedRunner.getLives(), wasLastKilledByHunter), player);
            }
        });
    }

    public static List<Player> getRemainingSpeedRunners()
    {
        List<Player> ret = new ArrayList<>();
        for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
        {
            player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
                    player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                    {
                        if (playerSpeedRunner.getLives() > 0 && !playerHunter.isHunter()) // Does the player have lives remaining and is not a hunter?
                        {
                            ret.add(player); // A player is still considered a speed runner if they have lives remaining and are not a hunter
                        }
                    }));
        }

        return ret;
    }

    private int lives; // How many lives the speed runner has
    private boolean wasLastKilledByHunter; // If their last death was from the Hunter

    public PlayerSpeedRunner()
    {
        this.lives = RMGameState.SPEED_RUNNER_LIVES; // Player starts with max amount of lives
        this.wasLastKilledByHunter = false; // Player shouldn't have been killed by hunter by default
    }

    public void setLives(int lives) {
        this.lives = Math.max(MIN_LIVES, Math.min(lives, RMGameState.SPEED_RUNNER_LIVES));
    }

    public void subtractLife()
    {
        this.setLives(this.getLives() - 1);
    }

    public int getLives() {
        return this.lives;
    }

    public void setWasLastKilledByHunter(boolean wasLastKilledByHunter)
    {
        this.wasLastKilledByHunter = wasLastKilledByHunter;
    }

    public boolean getWasLastKilledByHunter()
    {
        return this.wasLastKilledByHunter;
    }

    public void copyFrom(PlayerSpeedRunner source)
    {
        this.lives = source.lives;
        this.wasLastKilledByHunter = source.wasLastKilledByHunter;
    }

    public void saveNBTData(CompoundTag nbt)
    {
        nbt.putInt("speed_runner_lives", this.lives);
        nbt.putBoolean("speed_runner_killed_by_hunter", this.wasLastKilledByHunter);
    }

    public void loadNBTData(CompoundTag nbt)
    {
        this.lives = nbt.getInt("speed_runner_lives");
        this.wasLastKilledByHunter = nbt.getBoolean("speed_runner_killed_by_hunter");
    }
}
