package net.laserdiamond.reversemanhunt.capability.hunter;

import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.capability.AbstractCapability;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID)
public class PlayerHunterCapability extends AbstractCapability<PlayerHunter> {

    public static Capability<PlayerHunter> PLAYER_HUNTER = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player player)
        {
            if (!player.getCapability(PLAYER_HUNTER).isPresent())
            {
                event.addCapability(ReverseManhunt.fromRMPath("hunter"), new PlayerHunterCapability(player));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event)
    {
        if (event.isWasDeath())
        {
            event.getOriginal().reviveCaps(); // Revive capability

            event.getOriginal().getCapability(PLAYER_HUNTER).ifPresent(oldPlayerHunter ->
            {
                event.getEntity().getCapability(PLAYER_HUNTER).ifPresent(newPlayerHunter ->
                {
                    newPlayerHunter.copyFrom(oldPlayerHunter);
                });
            });

            event.getOriginal().invalidateCaps(); // Invalidate capability
        }
    }

    private PlayerHunter playerHunter = null;
    private final Player player;

    private PlayerHunterCapability(Player player)
    {
        this.player = player;
    }

    protected PlayerHunter createCapability()
    {
        if (this.playerHunter == null)
        {
            this.playerHunter = new PlayerHunter(this.player.getUUID());
        }
        return this.playerHunter;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_HUNTER)
        {
            return this.capabilityOptional.cast();
        }
        return LazyOptional.empty();
    }
}
