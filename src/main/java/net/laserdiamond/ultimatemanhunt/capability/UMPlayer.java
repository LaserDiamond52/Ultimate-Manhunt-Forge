package net.laserdiamond.ultimatemanhunt.capability;

import com.google.common.collect.HashMultimap;
import net.laserdiamond.laserutils.capability.AbstractCapabilityData;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.client.game.ClientGameTime;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.UMCapabilitySyncS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerMaxLifeChangeS2CPacket;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

@AutoRegisterCapability
public class UMPlayer extends AbstractCapabilityData<UMPlayer>
{
    private static final ResourceLocation ACTIVE_MODIFIER = UltimateManhunt.fromUMPath("attribute.active_hunter");

    public static final long TRACKER_RESET_COOLDOWN_TICKS = 20L;
    public static final int MIN_LIVES = 0;
    public static final int MAX_LIVES = 99;
    private static int currentMaxLives = 3;
    private static boolean buffedHunterOnFinalDeath = false;
    private static double maxHealthBonus = 0.5;
    private static AttributeModifier.Operation maxHealthModifier = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    private static double armorBonus = 5;
    private static AttributeModifier.Operation armorBonusModifier = AttributeModifier.Operation.ADD_VALUE;
    private static double movementSpeedBonus = 0.1;
    private static AttributeModifier.Operation movementSpeedBonusModifier = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    private static double movementEfficiencyBonus = 0.1;
    private static AttributeModifier.Operation movementEfficiencyModifier = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    private static double waterMovementEfficiencyBonus = 0.1;
    private static AttributeModifier.Operation waterMovementEfficiencyModifier = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    private static double miningEfficiencyBonus = 0.15;
    private static AttributeModifier.Operation miningEfficiencyModifier = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    private static double submergedMiningEfficiencyBonus = 0.15;
    private static AttributeModifier.Operation submergedMiningEfficiencyModifier = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    private static double attackDamageBonus = 0.25;
    private static AttributeModifier.Operation attackDamageBonusModifier = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    private static boolean hasInfiniteSaturation = false;
    private static float passiveRegen = 2;

    /**
     * @return The maximum amount of lives speed runners can currently hold
     */
    public static int getMaxLives()
    {
        return currentMaxLives;
    }

    /**
     * Sets the maximum amount of lives speed runners can currently hold
     * @param lives The new maximum amount of lives speed runners can hold
     * @return False if a Manhunt game has already been started. Returns true otherwise
     */
    public static boolean setMaxLives(int lives)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        currentMaxLives = Math.min(MAX_LIVES, Math.max(lives, 1));
        UMPackets.sendToAllClients(new SpeedRunnerMaxLifeChangeS2CPacket(lives));
        return true;
    }

    /**
     * @return Whether speed runners that die and become hunters also become buffed hunters
     */
    public static boolean getIsBuffedHunterOnFinalDeath()
    {
        return buffedHunterOnFinalDeath;
    }

    /**
     * Sets if speed runners that die and become hunters also become buffed hunters
     * @param isBuffedHunter Whether the new hunter becomes a buffed hunter
     * @return False if a Manhunt game has already been started. Returns true otherwise
     */
    public static boolean setIsBuffedHunterOnFinalDeath(boolean isBuffedHunter)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        buffedHunterOnFinalDeath = isBuffedHunter;
        return true;
    }

    public static double getMaxHealthBonus()
    {
        return maxHealthBonus;
    }

    public static AttributeModifier.Operation getMaxHealthModifier()
    {
        return maxHealthModifier;
    }

    public static double getArmorBonus()
    {
        return armorBonus;
    }

    public static AttributeModifier.Operation getArmorBonusModifier()
    {
        return armorBonusModifier;
    }

    public static double getMovementSpeedBonus()
    {
        return movementSpeedBonus;
    }

    public static AttributeModifier.Operation getMovementSpeedBonusModifier()
    {
        return movementSpeedBonusModifier;
    }

    public static double getMovementEfficiencyBonus()
    {
        return movementEfficiencyBonus;
    }

    public static AttributeModifier.Operation getMovementEfficiencyModifier()
    {
        return movementEfficiencyModifier;
    }

    public static double getWaterMovementEfficiencyBonus()
    {
        return waterMovementEfficiencyBonus;
    }

    public static AttributeModifier.Operation getWaterMovementEfficiencyModifier()
    {
        return waterMovementEfficiencyModifier;
    }

    public static double getMiningEfficiencyBonus()
    {
        return miningEfficiencyBonus;
    }

    public static AttributeModifier.Operation getMiningEfficiencyModifier()
    {
        return miningEfficiencyModifier;
    }

    public static double getSubmergedMiningEfficiencyBonus()
    {
        return submergedMiningEfficiencyBonus;
    }

    public static AttributeModifier.Operation getSubmergedMiningEfficiencyModifier()
    {
        return submergedMiningEfficiencyModifier;
    }

    public static double getAttackDamageBonus()
    {
        return attackDamageBonus;
    }

    public static AttributeModifier.Operation getAttackDamageBonusModifier()
    {
        return attackDamageBonusModifier;
    }

    public static boolean getHasInfiniteSaturation()
    {
        return hasInfiniteSaturation;
    }

    public static float getPassiveRegen()
    {
        return passiveRegen;
    }


    public static boolean setMaxHealthBonus(double newHealthBonus)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        maxHealthBonus = newHealthBonus;
        return true;
    }

    public static boolean setMaxHealthBonusModifier(AttributeModifier.Operation operation)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        maxHealthModifier = operation;
        return true;
    }

    public static boolean setArmorBonus(double newArmorBonus)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        armorBonus = newArmorBonus;
        return true;
    }

    public static boolean setArmorBonusModifier(AttributeModifier.Operation operation)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        armorBonusModifier = operation;
        return true;
    }

    public static boolean setMovementSpeedBonus(double newSpeedBonus)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        movementSpeedBonus = newSpeedBonus;
        return true;
    }

    public static boolean setMovementSpeedBonusModifier(AttributeModifier.Operation operation)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        movementSpeedBonusModifier = operation;
        return true;
    }

    public static boolean setMovementEfficiencyBonus(double value)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        movementEfficiencyBonus = value;
        return true;
    }

    public static boolean setMovementEfficiencyBonusModifier(AttributeModifier.Operation operation)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        movementEfficiencyModifier = operation;
        return true;
    }

    public static boolean setWaterMovementEfficiencyBonus(double value)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        waterMovementEfficiencyBonus = value;
        return true;
    }

    public static boolean setWaterMovementEfficiencyBonusModifier(AttributeModifier.Operation operation)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        waterMovementEfficiencyModifier = operation;
        return true;
    }

    public static boolean setMiningEfficiencyBonus(double value)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        miningEfficiencyBonus = value;
        return true;
    }

    public static boolean setMiningEfficiencyBonusModifier(AttributeModifier.Operation operation)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        miningEfficiencyModifier = operation;
        return true;
    }

    public static boolean setSubmergedMiningEfficiencyBonus(double value)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        submergedMiningEfficiencyBonus = value;
        return true;
    }

    public static boolean setSubmergedMiningEfficiencyBonusModifier(AttributeModifier.Operation operation)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        submergedMiningEfficiencyModifier = operation;
        return true;
    }

    public static boolean setAttackDamageBonus(double newAttackDamage)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        attackDamageBonus = newAttackDamage;
        return true;
    }

    public static boolean setAttackDamageBonusModifier(AttributeModifier.Operation operation)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        attackDamageBonusModifier = operation;
        return true;
    }

    public static boolean setHasInfiniteSaturation(boolean infiniteSaturation)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        hasInfiniteSaturation = infiniteSaturation;
        return true;
    }

    public static boolean setPassiveRegen(float amount)
    {
        if (UMGame.State.hasGameBeenStarted())
        {
            return false;
        }
        passiveRegen = Math.max(0, amount);
        return true;
    }

    /**
     * @return A {@link HashMultimap} of {@linkplain Attribute attributes} and {@linkplain AttributeModifier attribute modifiers} to be applied to each Hunter
     */
    public static HashMultimap<Holder<Attribute>, AttributeModifier> createHunterAttributes()
    {
        HashMultimap<Holder<Attribute>, AttributeModifier> ret = HashMultimap.create();
        ret.put(Attributes.MAX_HEALTH, new AttributeModifier(ACTIVE_MODIFIER, maxHealthBonus, maxHealthModifier));
        ret.put(Attributes.ARMOR, new AttributeModifier(ACTIVE_MODIFIER, armorBonus, armorBonusModifier));

        ret.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(ACTIVE_MODIFIER, movementSpeedBonus, movementSpeedBonusModifier));
        ret.put(Attributes.MOVEMENT_EFFICIENCY, new AttributeModifier(ACTIVE_MODIFIER, movementEfficiencyBonus, movementEfficiencyModifier));
        ret.put(Attributes.WATER_MOVEMENT_EFFICIENCY, new AttributeModifier(ACTIVE_MODIFIER, waterMovementEfficiencyBonus, waterMovementEfficiencyModifier));

        ret.put(Attributes.MINING_EFFICIENCY, new AttributeModifier(ACTIVE_MODIFIER, miningEfficiencyBonus, miningEfficiencyModifier));
        ret.put(Attributes.SUBMERGED_MINING_SPEED, new AttributeModifier(ACTIVE_MODIFIER, submergedMiningEfficiencyBonus, submergedMiningEfficiencyModifier));

        ret.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ACTIVE_MODIFIER, attackDamageBonus, attackDamageBonusModifier));

        return ret;
    }

    public static List<Player> getRemainingSpeedRunners()
    {
        List<Player> ret = new LinkedList<>();

        for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
        {
            player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                if (umPlayer.isSpeedRunner())
                {
                    ret.add(player);
                }
            });
        }
        return ret;
    }

    public static boolean isSpeedRunnerOnGracePeriodServer(Player player)
    {
        AtomicBoolean ret = new AtomicBoolean();
        player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
        {
            ret.set(umPlayer.isSpeedRunnerOnGracePeriodServer());
        });
        return ret.get();
    }

    /**
     * Gets all {@linkplain Player players} that are hunters
     * @param onlyBuffed Whether to gather only buffed hunters
     * @return A {@linkplain List list} of {@linkplain Player players} that are hunters or buffed hunters
     */
    public static List<Player> getHunters(boolean onlyBuffed)
    {
        List<Player> ret = new LinkedList<>();
        for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
        {
            player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                if (onlyBuffed && umPlayer.isBuffedHunter())
                {
                    ret.add(player);
                } else if (!onlyBuffed && umPlayer.isHunter())
                {
                    ret.add(player);
                }
            });
        }
        return ret;
    }

    /**
     * Gets all the {@linkplain Player player speed runners} available for the hunter to track
     * @param playerHunter The {@linkplain Player player} that is assumed to be a hunter
     * @return A {@link List} of {@linkplain Player players} available for the hunter to track
     */
    public static List<Player> getAvailableSpeedRunners(Player playerHunter)
    {
        List<Player> ret = new LinkedList<>(UMPlayer.getRemainingSpeedRunners()); // Get all the remaining speed runners
        Level hunterLvl = playerHunter.level();
        Iterator<Player> speedRunnerIterator = ret.iterator();
        while (speedRunnerIterator.hasNext()) // Use an iterator, since we want to remove elements from the list while iterating through it
        {
            Player player = speedRunnerIterator.next();
            Level speedRunnerLvl = player.level();
            if (playerHunter.getUUID() == player.getUUID())
            {
                // In the event that the player hunter is not assured to be a hunter, we don't want to track ourselves
                // Remove this player from the list
                speedRunnerIterator.remove();
                continue;
            }
            if (!hunterLvl.isClientSide && !speedRunnerLvl.isClientSide) // Ensure we are on the server
            {
                if (!hunterLvl.dimension().equals(speedRunnerLvl.dimension())) // Check if the dimensions do not match
                {
                    speedRunnerIterator.remove(); // Dimensions do not match. This is not a player we want to try and track
                }
            }
        }
        return ret;
    }

    public static List<Player> getSpectators()
    {
        List<Player> ret = new LinkedList<>();
        for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
        {
            player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                if (umPlayer.isSpectator())
                {
                    ret.add(player);
                }
            });
        }
        return ret;
    }

    public static void forAllPlayers(BiConsumer<Player, UMPlayer> forSpeedRunners, BiConsumer<Player, UMPlayer> forHunters, BiConsumer<Player, UMPlayer> forSpectators, BiConsumer<Player, UMPlayer> forAll)
    {
        for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
        {
            player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                forAll.accept(player, umPlayer);
                if (umPlayer.isSpeedRunner())
                {
                    forSpeedRunners.accept(player, umPlayer);
                } else if (umPlayer.isHunter())
                {
                    forHunters.accept(player, umPlayer);
                } else if (umPlayer.isSpectator())
                {
                    forSpectators.accept(player, umPlayer);
                } else
                {
                    forSpectators.accept(player, umPlayer);
                }
            });
        }
    }

    private int lives;
    private boolean wasLastKilledByHunter;
    private long gracePeriodTimeStamp;
    private long trackerResetCooldown;
    private boolean isBuffedHunter;
    private int trackingIndex;
    private UUID trackingPlayerUUID;
    private UMGame.PlayerRole role;

    public UMPlayer(UUID defaultTrackingUUID)
    {
        this.lives = getMaxLives();
        this.wasLastKilledByHunter = false;
        this.gracePeriodTimeStamp = 0;
        this.trackerResetCooldown = 0;
        this.isBuffedHunter = false;
        this.trackingIndex = -1; // Start at -1, since the UUID being tracked here is ourselves
        this.trackingPlayerUUID = defaultTrackingUUID; // Default the UUID of the player being tracked to ourselves
        this.role = UMGame.PlayerRole.SPEED_RUNNER;
    }

    @Override
    public void copyFrom(UMPlayer umPlayer)
    {
        this.lives = umPlayer.lives;
        this.wasLastKilledByHunter = umPlayer.wasLastKilledByHunter;
        this.gracePeriodTimeStamp = umPlayer.gracePeriodTimeStamp;
        this.trackerResetCooldown = umPlayer.trackerResetCooldown;
        this.isBuffedHunter = umPlayer.isBuffedHunter;
        this.trackingIndex = umPlayer.trackingIndex;
        this.trackingPlayerUUID = umPlayer.trackingPlayerUUID;
        this.role = umPlayer.role;
    }

    @Override
    public void saveNBTData(CompoundTag compoundTag)
    {
        compoundTag.putInt("speed_runner_lives", this.getLives());
        compoundTag.putBoolean("was_last_killed_by_hunter", this.isWasLastKilledByHunter());
        compoundTag.putLong("grace_period_time_stamp", this.getGracePeriodTimeStamp());
        compoundTag.putLong("tracker_reset_cooldown", this.getTrackerResetCooldown());
        compoundTag.putBoolean("is_buffed_hunter", this.isBuffedHunter());
        compoundTag.putInt("tracking_index", this.getTrackingIndex());
        compoundTag.putUUID("tracking_player_uuid", this.getTrackingPlayerUUID());
        compoundTag.putString("role", this.getRole().toString());
    }

    @Override
    public void loadNBTData(CompoundTag compoundTag)
    {
        this.lives = compoundTag.getInt("speed_runner_lives");
        this.wasLastKilledByHunter = compoundTag.getBoolean("was_last_killed_by_hunter");
        this.gracePeriodTimeStamp = compoundTag.getLong("grace_period_time_stamp");
        this.trackerResetCooldown = compoundTag.getLong("tracker_reset_cooldown");
        this.isBuffedHunter = compoundTag.getBoolean("is_buffed_hunter");
        this.trackingIndex = compoundTag.getInt("tracking_player_index");
        this.trackingPlayerUUID = compoundTag.getUUID("tracking_player_uuid");
        this.role = UMGame.PlayerRole.fromString(compoundTag.getString("role"));
    }

    public int getLives() {
        return lives;
    }

    public UMPlayer setLives(int lives) {
        this.lives = Math.max(MIN_LIVES, Math.min(lives, getMaxLives()));
        return this;
    }

    public UMPlayer subtractLife()
    {
        this.setLives(this.getLives() - 1);
        return this;
    }

    public boolean isWasLastKilledByHunter() {
        return wasLastKilledByHunter;
    }

    public UMPlayer setWasLastKilledByHunter(boolean wasLastKilledByHunter) {
        this.wasLastKilledByHunter = wasLastKilledByHunter;
        return this;
    }

    public long getGracePeriodTimeStamp() {
        return gracePeriodTimeStamp;
    }

    public UMPlayer setGracePeriodTimeStamp(long gracePeriodTimeStamp) {
        this.gracePeriodTimeStamp = Math.max(0, gracePeriodTimeStamp);
        return this;
    }

    public long getTrackerResetCooldown()
    {
        return this.trackerResetCooldown;
    }

    public UMPlayer resetTrackerCooldown(long gameTime)
    {
        this.trackerResetCooldown = TRACKER_RESET_COOLDOWN_TICKS + gameTime;
        return this;
    }

    public boolean isTrackerCooldownDone(long gameTime)
    {
        return this.trackerResetCooldown <= gameTime;
    }

    private boolean getIsBuffedHunter() {
        return isBuffedHunter;
    }

    public UMPlayer setBuffedHunter(boolean buffedHunter) {
        isBuffedHunter = buffedHunter;
        return this;
    }

    public int getTrackingIndex() {
        return trackingIndex;
    }

    private UMPlayer setTrackingIndex(int trackingIndex) {
        this.trackingIndex = trackingIndex;
        return this;
    }

    private void setPlayerToTrack(Player player)
    {
        this.trackingPlayerUUID = player.getUUID();
    }

    public UMPlayer setPlayerToTrack(int trackingIndex, Player player)
    {
        this.setTrackingIndex(trackingIndex);
        this.setPlayerToTrack(player);
        return this;
    }

    public UUID getTrackingPlayerUUID()
    {
        return this.trackingPlayerUUID;
    }


    public UMGame.PlayerRole getRole() {
        return role;
    }

    public UMPlayer setRole(UMGame.PlayerRole role) {
        this.role = role;
        return this;
    }

    public boolean isSpeedRunner()
    {
        return (this.getLives() > 0) && (this.getRole() == UMGame.PlayerRole.SPEED_RUNNER);
    }

    public boolean isHunter()
    {
        return this.getRole() == UMGame.PlayerRole.HUNTER;
    }

    public boolean isBuffedHunter()
    {
        return this.isHunter() && this.isBuffedHunter;
    }

    public boolean isSpectator()
    {
        return this.getRole() == UMGame.PlayerRole.SPECTATOR;
    }

    public boolean isSpeedRunnerOnGracePeriodServer()
    {
        return this.isSpeedRunner() && (UMGame.getCurrentGameTime() < this.getGracePeriodTimeStamp());
    }

    public boolean isSpeedRunnerOnGracePeriodClient()
    {
        return this.isSpeedRunner() && (ClientGameTime.getGameTime() < this.getGracePeriodTimeStamp());
    }

    public void reset(Player player, UMGame.PlayerRole role, boolean logPlayer)
    {
        if (logPlayer)
        {
            UMGame.logPlayerUUID(player);
        }
        this.setRole(role)
                .setLives(getMaxLives())
                .setWasLastKilledByHunter(false)
                .setGracePeriodTimeStamp(0)
                .sendUpdateFromServerToSelf(player);
    }

    public void resetToSpeedRunner(Player player, boolean logPlayer)
    {
        this.setBuffedHunter(false);
        this.reset(player, UMGame.PlayerRole.SPEED_RUNNER, logPlayer);
    }

    public void resetToHunter(Player player, boolean logPlayer)
    {
        UMGame.logPlayerUUID(player);
        this.reset(player, UMGame.PlayerRole.HUNTER, logPlayer);
    }

    public void resetToSpectator(Player player, boolean logPlayer)
    {
        this.reset(player, UMGame.PlayerRole.SPECTATOR, logPlayer);
    }

    public final void sendUpdateFromServer(Player trackedPlayer, Player receivingPlayer)
    {
        UMPackets.sendToAllTrackingEntity(new UMCapabilitySyncS2CPacket(trackedPlayer, this), receivingPlayer);
    }

    public final void sendUpdateFromServerToSelf(Player player)
    {
        UMPackets.sendToAllTrackingEntityAndSelf(new UMCapabilitySyncS2CPacket(player, this), player);
    }
}
