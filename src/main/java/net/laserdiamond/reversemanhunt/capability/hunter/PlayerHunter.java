package net.laserdiamond.reversemanhunt.capability.hunter;

import com.google.common.collect.HashMultimap;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.capability.AbstractCapabilityData;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunner;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@AutoRegisterCapability
public class PlayerHunter extends AbstractCapabilityData<PlayerHunter> {

    private static final ResourceLocation ACTIVE_MODIFIER = ReverseManhunt.fromRMPath("attribute.active_hunter");
    private static final ResourceLocation SPAWN_MODIFIER = ReverseManhunt.fromRMPath("attribute.spawn_hunter");


    /**
     * @return A {@link HashMultimap} of {@linkplain Attribute attributes} and {@linkplain AttributeModifier attribute modifiers} to be applied to each Hunter
     */
    public static HashMultimap<Holder<Attribute>, AttributeModifier> createHunterAttributes()
    {
        HashMultimap<Holder<Attribute>, AttributeModifier> ret = HashMultimap.create();
        ret.put(Attributes.MAX_HEALTH, new AttributeModifier(ACTIVE_MODIFIER, 10, AttributeModifier.Operation.ADD_VALUE));
        ret.put(Attributes.ARMOR, new AttributeModifier(ACTIVE_MODIFIER, 5, AttributeModifier.Operation.ADD_VALUE));

        ret.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(ACTIVE_MODIFIER, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        ret.put(Attributes.MOVEMENT_EFFICIENCY, new AttributeModifier(ACTIVE_MODIFIER, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        ret.put(Attributes.WATER_MOVEMENT_EFFICIENCY, new AttributeModifier(ACTIVE_MODIFIER, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

        ret.put(Attributes.MINING_EFFICIENCY, new AttributeModifier(ACTIVE_MODIFIER, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        ret.put(Attributes.SUBMERGED_MINING_SPEED, new AttributeModifier(ACTIVE_MODIFIER, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

        ret.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ACTIVE_MODIFIER, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

        return ret;
    }

    /**
     * @return A {@link HashMultimap} of {@linkplain Attribute attributes} and {@linkplain AttributeModifier attribute modifiers} to be applied to each Hunter upon starting the game
     */
    public static HashMultimap<Holder<Attribute>, AttributeModifier> createHunterSpawnAttributes()
    {
        HashMultimap<Holder<Attribute>, AttributeModifier> ret = HashMultimap.create();
        ret.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(SPAWN_MODIFIER, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        ret.put(Attributes.JUMP_STRENGTH, new AttributeModifier(SPAWN_MODIFIER, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        ret.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(SPAWN_MODIFIER, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        return ret;
    }

    public static List<Player> getHunters()
    {
        List<Player> ret = new ArrayList<>();
        for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
        {
            player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
            {
                if (playerHunter.isHunter()) // Is the player a hunter?
                {
                    ret.add(player); // Add player if they are a hunter
                }
            });
        }
        return ret;
    }

    public static List<Player> getBuffedHunters()
    {
        List<Player> ret = new ArrayList<>();
        for (Player player : getHunters())
        {
            player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
            {
                if (playerHunter.isBuffed()) // Is the player a hunter and buffed?
                {
                    ret.add(player); // Add player if they are a hunter and have buffs
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
        List<Player> ret = new ArrayList<>(PlayerSpeedRunner.getRemainingSpeedRunners()); // Get all the remaining speed runners
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

    private boolean hunter;
    private boolean buffed;
    private int trackingIndex;
    private UUID trackingPlayerUUID;

    public PlayerHunter(UUID defaultUUID)
    {
        this.hunter = false;
        this.buffed = false;
        this.trackingIndex = -1; // Start at -1, since the UUID being tracked here is ourselves
        this.trackingPlayerUUID = defaultUUID; // Default the UUID of the player being tracked to ourselves
    }

    public void setHunter(boolean isHunter)
    {
        this.hunter = isHunter;
    }

    public boolean isHunter()
    {
        return this.hunter;
    }

    public void setBuffed(boolean isBuffedHunter)
    {
        this.buffed = isBuffedHunter;
    }

    public boolean isBuffed()
    {
        return this.buffed;
    }

    private void setTrackingIndex(int trackingIndex)
    {
        this.trackingIndex = trackingIndex;
    }

    public int getTrackingIndex()
    {
        return this.trackingIndex;
    }

    private void setPlayerToTrack(Player player)
    {
        this.trackingPlayerUUID = player.getUUID();
    }

    public void setPlayerToTrack(int trackingIndex, Player player)
    {
        this.setTrackingIndex(trackingIndex);
        this.setPlayerToTrack(player);
    }

    public UUID getTrackingPlayerUUID()
    {
        return this.trackingPlayerUUID;
    }

    @Override
    public void copyFrom(PlayerHunter source)
    {
        this.hunter = source.hunter;
        this.buffed = source.buffed;
        this.trackingIndex = source.trackingIndex;
        this.trackingPlayerUUID = source.trackingPlayerUUID;
    }

    @Override
    public void saveNBTData(CompoundTag nbt)
    {
        nbt.putBoolean("is_hunter", this.isHunter());
        nbt.putBoolean("is_buffed_hunter", this.isBuffed());
        nbt.putInt("tracking_index", this.getTrackingIndex());
        nbt.putUUID("tracking_player_uuid", this.getTrackingPlayerUUID());
    }

    @Override
    public void loadNBTData(CompoundTag nbt)
    {
        this.hunter = nbt.getBoolean("is_hunter");
        this.buffed = nbt.getBoolean("is_buffed_hunter");
        this.trackingIndex = nbt.getInt("tracking_index");
        this.trackingPlayerUUID = nbt.getUUID("tracking_player_uuid");
    }
}
