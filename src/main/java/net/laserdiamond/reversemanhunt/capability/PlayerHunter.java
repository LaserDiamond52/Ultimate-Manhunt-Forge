package net.laserdiamond.reversemanhunt.capability;

import com.google.common.collect.HashMultimap;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterChangeC2SPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterChangeS2CPacket;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegisterCapability
public class PlayerHunter {

    private static final ResourceLocation ACTIVE_MODIFIER = ReverseManhunt.fromRMPath("attribute.active_hunter");

    private static final ResourceLocation SPAWN_MODIFIER = ReverseManhunt.fromRMPath("attribute.spawn_hunter");


    /**
     * @return A {@link HashMultimap} of {@linkplain Attribute attributes} and {@linkplain AttributeModifier attribute modifiers} to be applied to each Hunter
     */
    public static HashMultimap<Holder<Attribute>, AttributeModifier> createHunterAttributes()
    {
        HashMultimap<Holder<Attribute>, AttributeModifier> ret = HashMultimap.create();
        ret.put(Attributes.MAX_HEALTH, new AttributeModifier(ACTIVE_MODIFIER, 20, AttributeModifier.Operation.ADD_VALUE));
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
        for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
        {
            player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
            {
                if (playerHunter.isHunter() && playerHunter.isBuffed()) // Is the player a hunter and buffed?
                {
                    ret.add(player); // Add player if they are a hunter and have buffs
                }
            });
        }
        return ret;
    }

    private boolean hunter;
    private boolean buffed;

    public PlayerHunter()
    {
        this.hunter = false;
        this.buffed = false;
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

    public void copyFrom(PlayerHunter source)
    {
        this.hunter = source.hunter;
        this.buffed = source.buffed;
    }

    public void saveNBTData(CompoundTag nbt)
    {
        nbt.putBoolean("is_hunter", this.hunter);
        nbt.putBoolean("is_buffed_hunter", this.buffed);
    }

    public void loadNBTData(CompoundTag nbt)
    {
        this.hunter = nbt.getBoolean("is_hunter");
        this.buffed = nbt.getBoolean("is_buffed_hunter");
    }

    public CompoundTag toNBT()
    {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("is_hunter", this.hunter);
        tag.putBoolean("is_buffed_hunter", this.buffed);
        return tag;
    }
}
