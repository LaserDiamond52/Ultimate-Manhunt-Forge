package net.laserdiamond.reversemanhunt.capability.game;

import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.capability.AbstractCapability;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID)
public class PlayerGameTimeCapability extends AbstractCapability<PlayerGameTime> {

    public static Capability<PlayerGameTime> PLAYER_GAME_TIME = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player player)
        {
            if (!player.getCapability(PLAYER_GAME_TIME).isPresent())
            {
                event.addCapability(ReverseManhunt.fromRMPath("rm_player_game_time"), new PlayerGameTimeCapability());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event)
    {
        if (event.isWasDeath())
        {
            event.getOriginal().reviveCaps();

            event.getOriginal().getCapability(PLAYER_GAME_TIME).ifPresent(oldGameTime ->
                    event.getEntity().getCapability(PLAYER_GAME_TIME).ifPresent(newGameTime ->
                            newGameTime.copyFrom(oldGameTime)));

            event.getOriginal().invalidateCaps();
        }
    }

    private PlayerGameTime playerGameTime = null;

    @Override
    protected PlayerGameTime createCapability()
    {
        if (this.playerGameTime == null)
        {
            this.playerGameTime = new PlayerGameTime();
        }
        return this.playerGameTime;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_GAME_TIME)
        {
            return this.capabilityOptional.cast();
        }
        return LazyOptional.empty();
    }
}
