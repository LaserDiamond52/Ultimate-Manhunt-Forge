package net.laserdiamond.reversemanhunt.capability.speedrunner;

import net.laserdiamond.reversemanhunt.RMGame;
import net.laserdiamond.reversemanhunt.capability.AbstractCapabilityData;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunterCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@AutoRegisterCapability
public class PlayerSpeedRunner extends AbstractCapabilityData<PlayerSpeedRunner> {

    public static final int MIN_LIVES = 0; // Minimum amount of lives a speed runner can have
    public static final boolean BUFFED_HUNTER_ON_FINAL_DEATH = false; // Does the speed runner become a buffed hunter after losing their final life?
    private static int maxLives = 3; // The player's max lives
    public static final int MAX_LIVES = 5; // The maximum lives allowed by the game


    /**
     * @return Gets the speed runner's maximum amount of lives
     */
    public static int getMaxLives()
    {
        return maxLives;
    }

    /**
     * Sets the maximum amount of lives a speed runner can have
     * @param lives The maximum amount of lives a speed runner can have. Cannot be greater than {@link #MAX_LIVES} or less than 1
     */
    public static void setMaxLives(int lives)
    {
        maxLives = Math.min(MAX_LIVES, Math.max(lives, 1));
    }

    public static List<Player> getRemainingSpeedRunners()
    {
        List<Player> ret = new ArrayList<>();
        // Get all online players
        for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
        {
            player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
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

    public static boolean isSpeedRunnerOnGracePeriod(Player player)
    {
        AtomicBoolean ret = new AtomicBoolean();
        player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
        {
            player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
            {
                if (!playerHunter.isHunter() && playerSpeedRunner.getWasLastKilledByHunter() && RMGame.isSpeedRunnerOnGracePeriod(player))
                {
                    ret.set(true);
                }
            });
        });
        return ret.get();
    }

    private int lives; // How many lives the speed runner has
    private boolean wasLastKilledByHunter; // If their last death was from the Hunter
    private long gracePeriodTimeStamp; // Game time stamp of when a speed runner's grace period is over

    public PlayerSpeedRunner()
    {
        this.lives = getMaxLives(); // Player starts with max amount of lives
        this.wasLastKilledByHunter = false; // Player shouldn't have been killed by hunter by default
        this.gracePeriodTimeStamp = 0;
    }

    public void setLives(int lives) {
        this.lives = Math.max(MIN_LIVES, Math.min(lives, getMaxLives()));
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

    public void setGracePeriodTimeStamp(long gracePeriodTimeStamp)
    {
        this.gracePeriodTimeStamp = Math.max(0, gracePeriodTimeStamp);
    }

    public long getGracePeriodTimeStamp()
    {
        return this.gracePeriodTimeStamp;
    }

    @Override
    public void copyFrom(PlayerSpeedRunner source)
    {
        this.lives = source.lives;
        this.wasLastKilledByHunter = source.wasLastKilledByHunter;
        this.gracePeriodTimeStamp = source.gracePeriodTimeStamp;
    }

    @Override
    public void saveNBTData(CompoundTag nbt)
    {
        nbt.putInt("speed_runner_lives", this.getLives());
        nbt.putBoolean("speed_runner_killed_by_hunter", this.getWasLastKilledByHunter());
        nbt.putLong("speed_runner_grace_period_time_stamp", this.getGracePeriodTimeStamp());
    }

    @Override
    public void loadNBTData(CompoundTag nbt)
    {
        this.lives = nbt.getInt("speed_runner_lives");
        this.wasLastKilledByHunter = nbt.getBoolean("speed_runner_killed_by_hunter");
        this.gracePeriodTimeStamp = nbt.getLong("speed_runner_grace_period_time_stamp");
    }
}
